from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
import logging
import time
import uuid
from datetime import datetime

from app.schemas import (
    BikeLocationBatchRequest,
    TidalStatsResponse,
    TidalAlertRequest
)
from app.services.geofence_service import GeofenceService
from app.services.tidal_stats_service import TidalStatsService
from app.services.alert_push_client import AlertPushClient

logger = logging.getLogger(__name__)

router = APIRouter()
geofence_service = GeofenceService()
tidal_service = TidalStatsService(geofence_service)
alert_client = AlertPushClient()


@router.post("/batch-report", summary="批量上报车辆位置")
def batch_report_locations(request: BikeLocationBatchRequest):
    try:
        success_count = tidal_service.update_locations(request.locations)
        return {
            "success": True,
            "message": f"成功更新 {success_count} 辆车的位置",
            "updated_count": success_count,
            "total_received": len(request.locations),
            "skipped_count": len(request.locations) - success_count
        }
    except Exception as e:
        logger.exception(f"批量上报位置异常: {e}")
        raise HTTPException(status_code=500, detail="位置上报服务内部错误")


@router.get("/stats", response_model=TidalStatsResponse, summary="获取潮汐统计数据")
def get_tidal_stats():
    try:
        stats = tidal_service.calculate_stats()
        return stats
    except Exception as e:
        logger.exception(f"获取潮汐统计异常: {e}")
        raise HTTPException(status_code=500, detail="获取统计数据失败")


@router.post("/trigger-check", summary="主动触发潮汐检查并推送预警")
def trigger_tidal_check():
    try:
        stats = tidal_service.calculate_stats()
        current_time = time.time()
        pushed_alerts: List[Dict[str, Any]] = []
        skipped_alerts: List[Dict[str, Any]] = []

        overloaded = [s for s in stats.stats if s.is_overloaded]

        suggested_target = None
        non_overloaded = [s for s in stats.stats if not s.is_overloaded and s.max_capacity > 0]
        if non_overloaded:
            suggested_target = min(non_overloaded, key=lambda x: x.overload_ratio)

        for fence_stats in overloaded:
            try:
                if not tidal_service.should_trigger_alert(fence_stats.fence_id, current_time):
                    skipped_alerts.append({
                        "fence_id": fence_stats.fence_id,
                        "fence_name": fence_stats.name,
                        "reason": "5分钟内已推送过预警，跳过"
                    })
                    continue

                alert = TidalAlertRequest(
                    alert_id=f"ALERT-{uuid.uuid4().hex[:16].upper()}",
                    source_fence_id=fence_stats.fence_id,
                    source_fence_name=fence_stats.name,
                    bike_count=fence_stats.bike_count,
                    max_capacity=fence_stats.max_capacity,
                    overload_ratio=fence_stats.overload_ratio,
                    suggested_target_fence_id=suggested_target.fence_id if suggested_target else "",
                    suggested_target_fence_name=suggested_target.name if suggested_target else "",
                    timestamp=datetime.now().isoformat()
                )

                response = alert_client.push_alert(alert)
                if response:
                    pushed_alerts.append({
                        "alert_id": response.alert_id,
                        "accepted": response.accepted,
                        "message": response.message,
                        "assigned_courier_name": response.assigned_courier_name,
                        "dispatch_order_id": response.dispatch_order_id,
                        "source_fence": fence_stats.name
                    })
                else:
                    pushed_alerts.append({
                        "alert_id": alert.alert_id,
                        "accepted": False,
                        "message": "推送失败或服务降级",
                        "source_fence": fence_stats.name
                    })

            except Exception as e:
                logger.error(f"处理围栏 {fence_stats.fence_id} 预警异常: {e}")
                continue

        return {
            "success": True,
            "message": f"检查完成，发现 {len(overloaded)} 个超载围栏，成功推送 {len(pushed_alerts)} 个预警，跳过 {len(skipped_alerts)} 个",
            "total_bikes": stats.total_bikes,
            "total_fences": stats.total_fences,
            "overloaded_fences": stats.overloaded_fences,
            "pushed_alerts": pushed_alerts,
            "skipped_alerts": skipped_alerts
        }
    except Exception as e:
        logger.exception(f"触发潮汐检查异常: {e}")
        raise HTTPException(status_code=500, detail="潮汐检查服务内部错误")
