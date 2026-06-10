package com.greenride.dispatch.controller;

import com.greenride.dispatch.dto.BikeLocationReportDTO;
import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.service.BikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> reportLocation(@RequestBody BikeLocationReportDTO dto) {
        boolean success = bikeService.reportLocation(dto);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("bikeId", dto.getBikeId());
        result.put("message", success ? "坐标上报成功" : "坐标上报失败");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{bikeId}")
    public ResponseEntity<Bike> getBikeById(@PathVariable String bikeId) {
        return bikeService.getBikeById(bikeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
