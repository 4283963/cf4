package com.greenride.dispatch.controller;

import com.greenride.dispatch.dto.BikeLocationReportDTO;
import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.service.BikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @PostMapping("/location/report")
    public ResponseEntity<Map<String, Object>> reportLocation(
            @Valid @RequestBody BikeLocationReportDTO dto) {

        BikeService.LocationReportResult result = bikeService.reportLocation(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isLocationSaved());
        response.put("bikeId", result.getBikeId());
        response.put("locationSaved", result.isLocationSaved());
        response.put("fenceChecked", result.isFenceChecked());
        response.put("insideFence", result.isInsideFence());
        response.put("ticketCreated", result.isTicketCreated());
        response.put("fenceMessage", result.getFenceMessage());
        response.put("message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bikeId}")
    public ResponseEntity<Bike> getBikeById(@PathVariable String bikeId) {
        return bikeService.getBikeById(bikeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
