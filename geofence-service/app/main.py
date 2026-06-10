from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from fastapi import status
from app.routers import geofence
import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="绿行共享单车 - 电子围栏校验服务",
    description="校园共享单车电子围栏规范停放校验系统",
    version="1.0.0"
)

app.include_router(geofence.router, prefix="/api/geofence", tags=["电子围栏"])


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    errors = []
    for err in exc.errors():
        loc = " -> ".join(str(x) for x in err.get("loc", []))
        errors.append({"field": loc, "message": err.get("msg", "")})
    logger.warning(f"请求参数校验失败: {errors}")
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={
            "detail": "请求参数不合法",
            "code": "VALIDATION_ERROR",
            "errors": errors
        }
    )


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.exception(f"全局未捕获异常: {exc}")
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "detail": "服务内部错误，请稍后重试",
            "code": "INTERNAL_ERROR"
        }
    )


@app.get("/health")
def health_check():
    return {"status": "ok", "service": "geofence-service"}
