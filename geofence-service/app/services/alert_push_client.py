import requests
import os
import logging
from typing import Optional

from app.schemas import TidalAlertRequest, TidalAlertResponse

logger = logging.getLogger(__name__)


class AlertPushClient:
    def __init__(self):
        self._base_url = os.getenv("DISPATCH_SERVICE_URL", "http://localhost:8080/api")
        self._timeout = 3

    def push_alert(self, alert: TidalAlertRequest) -> Optional[TidalAlertResponse]:
        try:
            url = f"{self._base_url}/tidal-alerts"
            payload = alert.model_dump()

            logger.info(f"推送潮汐预警到 {url}, alert_id={alert.alert_id}")

            response = requests.post(
                url,
                json=payload,
                timeout=self._timeout,
                headers={"Content-Type": "application/json"}
            )

            if response.status_code == 200:
                try:
                    data = response.json()
                    return TidalAlertResponse(**data)
                except Exception as e:
                    logger.warning(f"解析预警响应失败: {e}")
                    return None
            else:
                logger.warning(f"推送预警失败，状态码: {response.status_code}, 响应: {response.text}")
                return None

        except requests.Timeout:
            logger.warning(f"推送预警超时 alert_id={alert.alert_id}")
            return None
        except requests.ConnectionError:
            logger.warning(f"推送预警连接失败 alert_id={alert.alert_id}")
            return None
        except Exception as e:
            logger.error(f"推送预警异常 alert_id={alert.alert_id}: {e}")
            return None
