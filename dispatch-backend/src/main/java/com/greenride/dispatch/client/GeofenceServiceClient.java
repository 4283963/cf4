package com.greenride.dispatch.client;

import com.greenride.dispatch.dto.GeofenceCheckRequest;
import com.greenride.dispatch.dto.GeofenceCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.SocketTimeoutException;

@Component
public class GeofenceServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(GeofenceServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${geofence.service.url}")
    private String geofenceServiceUrl;

    public GeofenceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeofenceCheckResponse checkGeofence(String bikeId, Double lng, Double lat) {
        if (lng == null || lat == null) {
            logger.warn("围栏校验坐标为空，跳过调用 bikeId={}", bikeId);
            return buildFallbackResponse(bikeId, "坐标参数为空，跳过围栏校验");
        }
        if (lng < -180.0 || lng > 180.0 || lat < -90.0 || lat > 90.0) {
            logger.warn("围栏校验坐标超出有效范围，跳过调用 bikeId={}, lng={}, lat={}", bikeId, lng, lat);
            return buildFallbackResponse(bikeId, "坐标超出有效范围，跳过围栏校验");
        }

        GeofenceCheckRequest request = new GeofenceCheckRequest();
        request.setBikeId(bikeId);
        GeofenceCheckRequest.PointDTO point = new GeofenceCheckRequest.PointDTO(lng, lat);
        request.setLocation(point);

        String url = geofenceServiceUrl + "/check";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeofenceCheckRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GeofenceCheckResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeofenceCheckResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.warn("围栏服务返回参数错误 bikeId={}, status={}", bikeId, response.getStatusCode());
                return buildFallbackResponse(bikeId, "坐标数据校验失败，无法完成围栏校验");
            }

            logger.warn("围栏服务返回非预期状态 bikeId={}, status={}", bikeId, response.getStatusCode());
            return buildFallbackResponse(bikeId, "围栏服务响应异常，降级处理");

        } catch (HttpClientErrorException e) {
            logger.warn("围栏服务客户端错误 bikeId={}, status={}, msg={}", bikeId, e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return buildFallbackResponse(bikeId, "坐标数据校验失败，无法完成围栏校验");
            }
            return buildFallbackResponse(bikeId, "围栏服务请求异常，降级处理");

        } catch (HttpServerErrorException e) {
            logger.error("围栏服务端错误 bikeId={}, status={}", bikeId, e.getStatusCode());
            return buildFallbackResponse(bikeId, "围栏服务暂不可用，降级处理");

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                logger.error("围栏服务调用超时 bikeId={}", bikeId);
                return buildFallbackResponse(bikeId, "围栏服务调用超时，降级处理");
            }
            logger.error("围栏服务不可达 bikeId={}, msg={}", bikeId, e.getMessage());
            return buildFallbackResponse(bikeId, "围栏服务连接失败，降级处理");

        } catch (RestClientException e) {
            logger.error("围栏服务调用异常 bikeId={}", bikeId, e);
            return buildFallbackResponse(bikeId, "围栏服务调用异常，降级处理");

        } catch (Exception e) {
            logger.error("围栏服务未知错误 bikeId={}", bikeId, e);
            return buildFallbackResponse(bikeId, "围栏校验未知错误，降级处理");
        }
    }

    private GeofenceCheckResponse buildFallbackResponse(String bikeId, String message) {
        GeofenceCheckResponse response = new GeofenceCheckResponse();
        response.setBikeId(bikeId);
        response.setIsInside(false);
        response.setMessage(message);
        return response;
    }
}
