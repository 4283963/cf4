import geopandas as gpd
from shapely.geometry import Point, Polygon
from shapely.ops import nearest_points
from typing import List, Dict, Optional, Tuple
import json
import os


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

        if os.path.exists(geojson_path):
            self._gdf = gpd.read_file(geojson_path)
        else:
            self._gdf = self._create_default_fences()
            os.makedirs(data_dir, exist_ok=True)
            self._gdf.to_file(geojson_path, driver="GeoJSON")

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

    def check_point_in_fence(self, lng: float, lat: float) -> Tuple[bool, Optional[Dict], Optional[Dict]]:
        point = Point(lng, lat)

        idx = self._gdf[self._gdf.contains(point)]
        if not idx.empty:
            fence = idx.iloc[0]
            distance = self._calculate_distance_to_edge(point, fence.geometry)
            info = {
                "fence_id": fence["fence_id"],
                "name": fence["name"],
                "type": fence["type"],
                "distance_to_edge": round(distance, 4)
            }
            return True, info, None

        nearest_idx = self._find_nearest_fence(point)
        if nearest_idx is not None:
            fence = self._gdf.loc[nearest_idx]
            distance = self._calculate_distance_to_edge(point, fence.geometry)
            info = {
                "fence_id": fence["fence_id"],
                "name": fence["name"],
                "type": fence["type"],
                "distance_to_edge": round(distance, 4)
            }
            return False, None, info

        return False, None, None

    def _find_nearest_fence(self, point: Point) -> Optional[int]:
        if self._gdf.empty:
            return None
        distances = self._gdf.geometry.distance(point)
        return distances.idxmin()

    def _calculate_distance_to_edge(self, point: Point, polygon: Polygon) -> float:
        boundary = polygon.boundary
        nearest_pts = nearest_points(point, boundary)
        return nearest_pts[0].distance(nearest_pts[1])

    def list_all_fences(self) -> List[Dict]:
        result = []
        for _, row in self._gdf.iterrows():
            result.append({
                "fence_id": row["fence_id"],
                "name": row["name"],
                "type": row["type"],
                "area": round(row.geometry.area, 6)
            })
        return result

    def get_fence_by_id(self, fence_id: str) -> Optional[Dict]:
        fence = self._gdf[self._gdf["fence_id"] == fence_id]
        if fence.empty:
            return None
        row = fence.iloc[0]
        return {
            "fence_id": row["fence_id"],
            "name": row["name"],
            "type": row["type"],
            "area": round(row.geometry.area, 6)
        }
