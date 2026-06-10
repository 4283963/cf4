from fastapi import FastAPI
from app.routers import geofence

app = FastAPI(
    title="绿行共享单车 - 电子围栏校验服务",
    description="校园共享单车电子围栏规范停放校验系统",
    version="1.0.0"
)

app.include_router(geofence.router, prefix="/api/geofence", tags=["电子围栏"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "geofence-service"}
