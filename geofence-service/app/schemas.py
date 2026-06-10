from pydantic import BaseModel, Field
from typing import List, Optional


class PointDTO(BaseModel):
    lng: float = Field(description="经度")
    lat: float = Field(description="纬度")


class GeofenceCheckRequest(BaseModel):
    bike_id: str = Field(description="单车编号")
    location: PointDTO = Field(description="车辆GPS坐标")


class FenceInfo(BaseModel):
    fence_id: str
    name: str
    type: str
    distance_to_edge: float


class GeofenceCheckResponse(BaseModel):
    bike_id: str
    is_inside: bool
    inside_fence: Optional[FenceInfo] = None
    nearest_fence: Optional[FenceInfo] = None
    message: str
