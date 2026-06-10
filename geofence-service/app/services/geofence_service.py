import geopandas as gpd
from shapely.geometry import Point, Polygon
from shapely.ops import nearest_points
from shapely.errors import ShapelyError
from typing import List, Dict, Optional, Tuple
import json
import os
import math
import logging

logger = logging.getLogger(__name__)


class InvalidCoordinateError(Exception):
    pass


class GeofenceService:
    _instance = None
    _gdf: gpd.GeoDataFrame = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load_fences()
        return cls._instance

    def _load_fences(self):
        data_dir = os.path.join(os.path.dirname(__file__), "data")
        geojson_path = os.path.join(data_dir, "campus_fences.geojson")

        try:
            if os.path.exists(geojson_path):
                self._gdf = gpd.read_file(geojson_path)
            else:
                self._gdf = self._create_default_fences()
                os.makedirs(data_dir, exist_ok=True)
                self._gdf.to_file(geojson_path, driver="GeoJSON")
        except Exception as e:
            logger.error(f"加载围栏数据失败，使用默认围栏: {e}")
            self._gdf = self._create_default_fences()

    def _create_default_fences(self) -> gpd.GeoDataFrame:
        fences = [
            {
                "fence_id": "F001",
                "name": "东门停车区",
                "type": "parking",
                "geometry": Polygon([
                    (116.4030, 39.9150),
                    (116.4045, 39.9150),
                    (116.4045, 39.9165),
                    (116.4030, 39.9165),
                    (116.4030, 39.9150)
                ])
            },
            {
                "fence_id": "F002",
                "name": "图书馆停车区",
                "type": "parking",
                "geometry": Polygon([
                    (116.4010, 39.9120),
                    (116.4025, 39.9120),
                    (116.4025, 39.9135),
                    (116.4010, 39.9135),
                    (116.4010, 39.9120)
                ])
            },
            {
                "fence_id": "F003",
                "name": "宿舍区停车区",
                "type": "parking",
                "geometry": Polygon([
                    (116.3980, 39.9170),
                    (116.4000, 39.9170),
                    (116.4000, 39.9190),
                    (116.3980, 39.9190),
                    (116.3980, 39.9170)
                ])
            },
            {
                "fence_id": "F004",
                "name": "教学楼停车区",
                "type": "parking",
                "geometry": Polygon([
                    (116.4050, 39.9100),
                    (116.4070, 39.9100),
                    (116.4070, 39.9120),
                    (116.4050, 39.9120),
                    (116.4050, 39.9100)
                ])
            },
            {
                "fence_id": "F005",
                "name": "体育馆停车区",
                "type": "parking",
                "geometry": Polygon([
                    (116.3990, 39.9090),
                    (116.4010, 39.9090),
                    (116.4010, 39.9105),
                    (116.3990, 39.9105),
                    (116.3990, 39.9090)
                ])
            }
        ]
        return gpd.GeoDataFrame(fences, crs="EPSG:4326")

    @staticmethod
    def _validate_coordinates(lng: float, lat: float) -> None:
        if lng is None or lat is None:
            raise InvalidCoordinateError("经纬度不能为空")
        if not isinstance(lng, (int, float)) or not isinstance(lat, (int, float)):
            raise InvalidCoordinateError("经纬度必须是数字类型")
        if math.isnan(lng) or math.isnan(lat) or math.isinf(lng) or math.isinf(lat):
            raise InvalidCoordinateError("经纬度包含无效值 (NaN/Infinity)")
        if lng < -180.0 or lng > 180.0:
            raise InvalidCoordinateError(f"经度超出有效范围: {lng}")
        if lat < -90.0 or lat > 90.0:
            raise InvalidCoordinateError(f"纬度超出有效范围: {lat}")

    def check_point_in_fence(self, lng: float, lat: float) -> Tuple[bool, Optional[Dict], Optional[Dict]]:
        self._validate_coordinates(lng, lat)

        try:
            point = Point(lng, lat)
        except (ShapelyError, TypeError, ValueError) as e:
            logger.error(f"创建 Point 对象失败 lng={lng}, lat={lat}: {e}")
            raise InvalidCoordinateError(f"坐标格式无效: {e}")

        if not point.is_valid or point.is_empty:
            raise InvalidCoordinateError("生成的坐标点无效或为空")

        try:
            idx = self._gdf[self._gdf.contains(point)]
        except Exception as e:
            logger.error(f"围栏包含判断异常: {e}")
            return False, None, None

        if not idx.empty:
            try:
                fence = idx.iloc[0]
                distance = self._calculate_distance_to_edge(point, fence.geometry)
                info = {
                    "fence_id": fence["fence_id"],
                    "name": fence["name"],
                    "type": fence["type"],
                    "distance_to_edge": round(distance, 4)
                }
                return True, info, None
            except Exception as e:
                logger.error(f"处理围栏内信息异常: {e}")
                return True, {
                    "fence_id": fence["fence_id"],
                    "name": fence["name"],
                    "type": fence["type"],
                    "distance_to_edge": 0.0
                }, None

        try:
            nearest_idx = self._find_nearest_fence(point)
        except Exception as e:
            logger.error(f"查找最近围栏异常: {e}")
            return False, None, None

        if nearest_idx is not None:
            try:
                fence = self._gdf.loc[nearest_idx]
                distance = self._calculate_distance_to_edge(point, fence.geometry)
                info = {
                    "fence_id": fence["fence_id"],
                    "name": fence["name"],
                    "type": fence["type"],
                    "distance_to_edge": round(distance, 4)
                }
                return False, None, info
            except Exception as e:
                logger.error(f"处理最近围栏信息异常: {e}")
                return False, None, None

        return False, None, None

    def _find_nearest_fence(self, point: Point) -> Optional[int]:
        if self._gdf.empty:
            return None
        try:
            valid_geoms = self._gdf[self._gdf.geometry.notna() & self._gdf.geometry.is_valid]
            if valid_geoms.empty:
                return None
            distances = valid_geoms.geometry.distance(point)
            if distances.empty:
                return None
            return distances.idxmin()
        except Exception as e:
            logger.error(f"计算最近围栏距离异常: {e}")
            return None

    def _calculate_distance_to_edge(self, point: Point, polygon: Polygon) -> float:
        try:
            if polygon is None or polygon.is_empty or not polygon.is_valid:
                return 0.0
            boundary = polygon.boundary
            if boundary is None or boundary.is_empty:
                return 0.0
            nearest_pts = nearest_points(point, boundary)
            if nearest_pts is None or len(nearest_pts) < 2:
                return 0.0
            dist = nearest_pts[0].distance(nearest_pts[1])
            return dist if not (math.isnan(dist) or math.isinf(dist)) else 0.0
        except Exception as e:
            logger.error(f"计算到边界距离异常: {e}")
            return 0.0

    def list_all_fences(self) -> List[Dict]:
        result = []
        try:
            for _, row in self._gdf.iterrows():
                try:
                    area_val = row.geometry.area if row.geometry is not None and not row.geometry.is_empty else 0.0
                    area_val = area_val if not (math.isnan(area_val) or math.isinf(area_val)) else 0.0
                    result.append({
                        "fence_id": row["fence_id"],
                        "name": row["name"],
                        "type": row["type"],
                        "area": round(area_val, 6)
                    })
                except Exception as e:
                    logger.warning(f"处理围栏信息异常: {e}")
                    continue
        except Exception as e:
            logger.error(f"列出围栏异常: {e}")
        return result

    def get_fence_by_id(self, fence_id: str) -> Optional[Dict]:
        try:
            fence = self._gdf[self._gdf["fence_id"] == fence_id]
            if fence.empty:
                return None
            row = fence.iloc[0]
            area_val = row.geometry.area if row.geometry is not None and not row.geometry.is_empty else 0.0
            area_val = area_val if not (math.isnan(area_val) or math.isinf(area_val)) else 0.0
            return {
                "fence_id": row["fence_id"],
                "name": row["name"],
                "type": row["type"],
                "area": round(area_val, 6)
            }
        except Exception as e:
            logger.error(f"查询围栏 {fence_id} 异常: {e}")
            return None
