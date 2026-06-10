package com.greenride.dispatch.service;

import com.greenride.dispatch.dto.BikeLocationReportDTO;
import com.greenride.dispatch.dto.GeofenceCheckResponse;
import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.client.GeofenceServiceClient;
import com.greenride.dispatch.repository.BikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BikeService {

    private static final Logger logger = LoggerFactory.getLogger(BikeService.class);

    private final BikeRepository bikeRepository;
    private final GeofenceServiceClient geofenceServiceClient;
    private final ViolationTicketService violationTicketService;

    public BikeService(BikeRepository bikeRepository,
                       GeofenceServiceClient geofenceServiceClient,
                       ViolationTicketService violationTicketService) {
        this.bikeRepository = bikeRepository;
        this.geofenceServiceClient = geofenceServiceClient;
        this.violationTicketService = violationTicketService;
    }

    @Transactional
    public LocationReportResult reportLocation(BikeLocationReportDTO dto) {
        LocationReportResult result = new LocationReportResult();
        result.setBikeId(dto.getBikeId());

        try {
            Bike bike = bikeRepository.findById(dto.getBikeId()).orElse(null);
            if (bike == null) {
                bike = new Bike();
                bike.setBikeId(dto.getBikeId());
                bike.setStatus("IN_USE");
                bike.setBikeType("STANDARD");
            }

            bike.setLongitude(dto.getLongitude());
            bike.setLatitude(dto.getLatitude());
            bike.setBatteryLevel(dto.getBatteryLevel());
            bike.setLastReportTime(LocalDateTime.now());
            bikeRepository.save(bike);
            result.setLocationSaved(true);
        } catch (Exception e) {
            logger.error("保存车辆坐标失败 bikeId={}", dto.getBikeId(), e);
            result.setLocationSaved(false);
            result.setMessage("车辆坐标保存失败");
            return result;
        }

        try {
            GeofenceCheckResponse fenceResult = geofenceServiceClient.checkGeofence(
                    dto.getBikeId(),
                    dto.getLongitude() != null ? dto.getLongitude().doubleValue() : null,
                    dto.getLatitude() != null ? dto.getLatitude().doubleValue() : null
            );

            result.setFenceChecked(true);
            result.setInsideFence(fenceResult.getIsInside());
            result.setFenceMessage(fenceResult.getMessage());

            if (!fenceResult.getIsInside() && isRealViolation(fenceResult)) {
                try {
                    violationTicketService.createViolationTicket(dto, fenceResult);
                    result.setTicketCreated(true);
                    result.setMessage("坐标上报成功，检测到违规停放，已生成罚款单");
                } catch (Exception e) {
                    logger.error("生成违规罚款单失败 bikeId={}", dto.getBikeId(), e);
                    result.setMessage("坐标上报成功，围栏校验完成，但罚款单生成失败");
                }
            } else if (fenceResult.getIsInside()) {
                result.setMessage("坐标上报成功，车辆停放在规范区域内");
            } else {
                result.setMessage("坐标上报成功，" + fenceResult.getMessage());
            }
        } catch (Exception e) {
            logger.error("围栏校验流程异常 bikeId={}", dto.getBikeId(), e);
            result.setFenceChecked(false);
            result.setMessage("坐标上报成功，但围栏校验服务暂不可用");
        }

        return result;
    }

    private boolean isRealViolation(GeofenceCheckResponse response) {
        if (response == null) {
            return false;
        }
        String msg = response.getMessage();
        if (msg == null) {
            return true;
        }
        return !(msg.contains("降级") || msg.contains("超时") || msg.contains("不可用")
                || msg.contains("失败") || msg.contains("跳过") || msg.contains("未知错误"));
    }

    public Optional<Bike> getBikeById(String bikeId) {
        try {
            return bikeRepository.findById(bikeId);
        } catch (Exception e) {
            logger.error("查询车辆信息失败 bikeId={}", bikeId, e);
            return Optional.empty();
        }
    }

    public static class LocationReportResult {
        private String bikeId;
        private boolean locationSaved;
        private boolean fenceChecked;
        private boolean insideFence;
        private boolean ticketCreated;
        private String fenceMessage;
        private String message;

        public String getBikeId() { return bikeId; }
        public void setBikeId(String bikeId) { this.bikeId = bikeId; }
        public boolean isLocationSaved() { return locationSaved; }
        public void setLocationSaved(boolean locationSaved) { this.locationSaved = locationSaved; }
        public boolean isFenceChecked() { return fenceChecked; }
        public void setFenceChecked(boolean fenceChecked) { this.fenceChecked = fenceChecked; }
        public boolean isInsideFence() { return insideFence; }
        public void setInsideFence(boolean insideFence) { this.insideFence = insideFence; }
        public boolean isTicketCreated() { return ticketCreated; }
        public void setTicketCreated(boolean ticketCreated) { this.ticketCreated = ticketCreated; }
        public String getFenceMessage() { return fenceMessage; }
        public void setFenceMessage(String fenceMessage) { this.fenceMessage = fenceMessage; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
