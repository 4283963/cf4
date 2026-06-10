import geopandas as gpd
from shapely.geometry import Point
from typing import List, Dict, Tuple, Optional
from datetime import datetime
import logging
import math
import time

from app.schemas import BikeLocationDTO, TidalStatsResponse, FenceStatsDTO
from app.services.geofence_service import GeofenceService

logger = logging.getLogger(__name__)


class TidalStatsService:
    def __init__(self, geofence_service: GeofenceService):
        self._geofence_service = geofence_service
        self._bike_locations: Dict[str, Tuple[float, float]] = {}
        self._last_alert_times: Dict[str, float] = {}

    def update_locations(self, locations: List[BikeLocationDTO]) -> int:
        success_count = 0
        try:
            for loc in locations:
                try:
                    lng = loc.lng
                    lat = loc.lat
                    if math.isnan(lng) or math.isnan(lat) or math.isinf(lng) or math.isinf(lat):
                        continue
                    if lng < -180.0 or lng > 180.0 or lat < -90.0 or lat > 90.0:
                        continue
                    self._bike_locations[loc.bike_id] = (lng, lat)
                    success_count += 1
                except Exception as e:
                    logger.warning(f"更新车辆位置失败 bike_id={loc.bike_id}: {e}")
                    continue
        except Exception as e:
            logger.error(f"批量更新车辆位置异常: {e}")
        return success_count

    def calculate_stats(self) -> TidalStatsResponse:
        timestamp = datetime.now().isoformat()
        total_bikes = len(self._bike_locations)
        total_fences = 0
        overloaded_fences = 0
        stats: List[FenceStatsDTO] = []
        suggested_target_fence: Optional[FenceStatsDTO] = None

        try:
            fences = self._geofence_service.get_all_fences_with_capacity()
            total_fences = len(fences)

            if total_fences == 0 or total_bikes == 0:
                return TidalStatsResponse(
                    total_bikes=total_bikes,
                    total_fences=total_fences,
                    overloaded_fences=0,
                    stats=[],
                    timestamp=timestamp
                )

            try:
                bike_data = []
                for bike_id, (lng, lat) in self._bike_locations.items():
                    bike_data.append({
                        "bike_id": bike_id,
                        "geometry": Point(lng, lat)
                    })
                bikes_gdf = gpd.GeoDataFrame(bike_data, crs="EPSG:4326")

                fence_data = []
                for fence in fences:
                    fence_data.append({
                        "fence_id": fence["fence_id"],
                        "name": fence["name"],
                        "max_capacity": fence["max_capacity"],
                        "geometry": fence["geometry"]
                    })
                fences_gdf = gpd.GeoDataFrame(fence_data, crs="EPSG:4326")

                if not bikes_gdf.empty and not fences_gdf.empty:
                    try:
                        fences_gdf.sindex
                        bikes_gdf.sindex
                        joined = gpd.sjoin(bikes_gdf, fences_gdf, how="left", predicate="within")

                        fence_bike_map: Dict[str, List[str]] = {}
                        for fence in fences:
                            fence_bike_map[fence["fence_id"]] = []

                        for _, row in joined.iterrows():
                            try:
                                if not math.isnan(row.get("index_right", float("nan"))):
                                    fence_id = fence_data[int(row["index_right"])]["fence_id"]
                                    fence_bike_map[fence_id].append(row["bike_id"])
                            except Exception as e:
                                logger.warning(f"处理关联结果异常: {e}")
                                continue
                    except Exception as e:
                        logger.error(f"空间关联分析异常: {e}")
                        fence_bike_map = {f["fence_id"]: [] for f in fences}
                else:
                    fence_bike_map = {f["fence_id"]: [] for f in fences}

                min_usage_ratio = float("inf")
                for fence in fences:
                    try:
                        fence_id = fence["fence_id"]
                        name = fence["name"]
                        max_capacity = int(fence["max_capacity"])
                        bike_ids = fence_bike_map.get(fence_id, [])
                        bike_count = len(bike_ids)

                        if max_capacity > 0:
                            overload_ratio = round(bike_count / max_capacity, 4)
                            usage_ratio = overload_ratio
                        else:
                            overload_ratio = 0.0
                            usage_ratio = 0.0

                        is_overloaded = bike_count > max_capacity

                        if is_overloaded:
                            overloaded_fences += 1

                        if not is_overloaded and usage_ratio < min_usage_ratio:
                            min_usage_ratio = usage_ratio
                            suggested_target_fence = FenceStatsDTO(
                                fence_id=fence_id,
                                name=name,
                                bike_count=bike_count,
                                max_capacity=max_capacity,
                                overload_ratio=overload_ratio,
                                is_overloaded=is_overloaded,
                                bike_ids=bike_ids
                            )

                        stats.append(FenceStatsDTO(
                            fence_id=fence_id,
                            name=name,
                            bike_count=bike_count,
                            max_capacity=max_capacity,
                            overload_ratio=overload_ratio,
                            is_overloaded=is_overloaded,
                            bike_ids=bike_ids
                        ))
                    except Exception as e:
                        logger.warning(f"处理围栏 {fence.get('fence_id')} 统计异常: {e}")
                        continue

            except Exception as e:
                logger.error(f"统计计算异常: {e}")

        except Exception as e:
            logger.error(f"计算潮汐统计异常: {e}")

        return TidalStatsResponse(
            total_bikes=total_bikes,
            total_fences=total_fences,
            overloaded_fences=overloaded_fences,
            stats=stats,
            timestamp=timestamp
        )

    def get_all_bike_locations(self) -> Dict[str, Tuple[float, float]]:
        try:
            return self._bike_locations.copy()
        except Exception as e:
            logger.error(f"获取车辆位置异常: {e}")
            return {}

    def should_trigger_alert(self, fence_id: str, current_time: float) -> bool:
        try:
            last_time = self._last_alert_times.get(fence_id, 0)
            if current_time - last_time < 300:
                return False
            self._last_alert_times[fence_id] = current_time
            return True
        except Exception as e:
            logger.error(f"报警防抖判断异常 fence_id={fence_id}: {e}")
            return True
