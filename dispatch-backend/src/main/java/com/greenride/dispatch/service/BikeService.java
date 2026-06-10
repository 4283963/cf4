package com.greenride.dispatch.service;

import com.greenride.dispatch.dto.BikeLocationReportDTO;
import com.greenride.dispatch.dto.GeofenceCheckResponse;
import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.entity.ViolationTicket;
import com.greenride.dispatch.client.GeofenceServiceClient;
import com.greenride.dispatch.repository.BikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BikeService {

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
    public boolean reportLocation(BikeLocationReportDTO dto) {
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

        try {
            GeofenceCheckResponse fenceResult = geofenceServiceClient.checkGeofence(
                    dto.getBikeId(),
                    dto.getLongitude().doubleValue(),
                    dto.getLatitude().doubleValue()
            );

            if (!fenceResult.getIsInside()) {
                violationTicketService.createViolationTicket(dto, fenceResult);
            }
        } catch (Exception e) {
            System.err.println("调用电子围栏服务失败: " + e.getMessage());
        }

        return true;
    }

    public Optional<Bike> getBikeById(String bikeId) {
        return bikeRepository.findById(bikeId);
    }
}
