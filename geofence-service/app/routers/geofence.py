from fastapi import APIRouter, HTTPException
from app.schemas import GeofenceCheckRequest, GeofenceCheckResponse, FenceInfo
from app.services.geofence_service import GeofenceService
from typing import List

router = APIRouter()
service = GeofenceService()


@router.post("/check", response_model=GeofenceCheckResponse, summary="校验单车是否在电子围栏内")
def check_geofence(request: GeofenceCheckRequest):
    is_inside, inside_fence, nearest_fence = service.check_point_in_fence(
        request.location.lng,
        request.location.lat
    )

    if is_inside:
        message = f"车辆停放在「{inside_fence['name']}」规范区域内"
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


@router.get("/fences", summary="查询所有电子围栏列表")
def list_fences():
    fences = service.list_all_fences()
    return {"total": len(fences), "fences": fences}


@router.get("/fences/{fence_id}", summary="根据ID查询电子围栏详情")
def get_fence(fence_id: str):
    fence = service.get_fence_by_id(fence_id)
    if not fence:
        raise HTTPException(status_code=404, detail="围栏不存在")
    return fence
