from fastapi import APIRouter, HTTPException, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from app.schemas import GeofenceCheckRequest, GeofenceCheckResponse, FenceInfo
from app.services.geofence_service import GeofenceService, InvalidCoordinateError
from typing import List
import logging

logger = logging.getLogger(__name__)

router = APIRouter()
service = GeofenceService()


@router.post("/check", response_model=GeofenceCheckResponse, summary="校验单车是否在电子围栏内")
def check_geofence(request: GeofenceCheckRequest):
    try:
        is_inside, inside_fence, nearest_fence = service.check_point_in_fence(
            request.location.lng,
            request.location.lat
        )

        if is_inside:
            message = f"车辆停放在「{inside_fence['name']}」规范区域内" if inside_fence else "车辆停放在规范区域内"
        else:
            if nearest_fence:
                message = f"车辆未停放在规范区域内，距离最近的「{nearest_fence['name']}」约 {nearest_fence['distance_to_edge']:.4f} 度"
            else:
                message = "未找到附近的围栏区域"

        return GeofenceCheckResponse(
            bike_id=request.bike_id,
            is_inside=is_inside,
            inside_fence=FenceInfo(**inside_fence) if inside_fence else None,
            nearest_fence=FenceInfo(**nearest_fence) if nearest_fence else None,
            message=message
        )
    except InvalidCoordinateError as e:
        logger.warning(f"坐标校验失败 bike_id={request.bike_id}: {e}")
        raise HTTPException(status_code=400, detail=f"无效的坐标数据: {str(e)}")
    except Exception as e:
        logger.exception(f"围栏校验内部错误 bike_id={request.bike_id}: {e}")
        raise HTTPException(status_code=500, detail="围栏校验服务内部错误")


@router.get("/fences", summary="查询所有电子围栏列表")
def list_fences():
    try:
        fences = service.list_all_fences()
        return {"total": len(fences), "fences": fences}
    except Exception as e:
        logger.exception(f"查询围栏列表错误: {e}")
        raise HTTPException(status_code=500, detail="查询围栏列表失败")


@router.get("/fences/{fence_id}", summary="根据ID查询电子围栏详情")
def get_fence(fence_id: str):
    try:
        fence = service.get_fence_by_id(fence_id)
        if not fence:
            raise HTTPException(status_code=404, detail="围栏不存在")
        return fence
    except HTTPException:
        raise
    except Exception as e:
        logger.exception(f"查询围栏详情错误 fence_id={fence_id}: {e}")
        raise HTTPException(status_code=500, detail="查询围栏详情失败")
