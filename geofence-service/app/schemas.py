from pydantic import BaseModel, Field, field_validator
from typing import List, Optional
import math


class PointDTO(BaseModel):
    lng: float = Field(description="经度")
    lat: float = Field(description="纬度")

    @field_validator("lng")
    @classmethod
    def validate_lng(cls, v):
        if v is None:
            raise ValueError("经度不能为空")
        if math.isnan(v) or math.isinf(v):
            raise ValueError("经度值无效")
        if v < -180.0 or v > 180.0:
            raise ValueError(f"经度必须在 -180 到 180 之间，当前值: {v}")
        return v

    @field_validator("lat")
    @classmethod
    def validate_lat(cls, v):
        if v is None:
            raise ValueError("纬度不能为空")
        if math.isnan(v) or math.isinf(v):
            raise ValueError("纬度值无效")
        if v < -90.0 or v > 90.0:
            raise ValueError(f"纬度必须在 -90 到 90 之间，当前值: {v}")
        return v


class GeofenceCheckRequest(BaseModel):
    bike_id: str = Field(description="单车编号", min_length=1, max_length=32)
    location: PointDTO = Field(description="车辆GPS坐标")

    @field_validator("bike_id")
    @classmethod
    def validate_bike_id(cls, v):
        if not v or not v.strip():
            raise ValueError("单车编号不能为空")
        return v.strip()


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
