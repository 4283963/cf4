package com.greenride.dispatch.controller;

import com.greenride.dispatch.entity.Courier;
import com.greenride.dispatch.service.CourierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private static final Logger logger = LoggerFactory.getLogger(CourierController.class);

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping
    public List<Courier> list() {
        try {
            return courierService.findAll();
        } catch (Exception e) {
            logger.error("查询小哥列表失败", e);
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Courier> getById(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Courier> courier = courierService.findById(id);
            return courier.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("查询小哥详情失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public Courier create(@RequestBody Courier courier) {
        try {
            if (courier == null) {
                return null;
            }
            return courierService.save(courier);
        } catch (Exception e) {
            logger.error("创建小哥失败", e);
            return null;
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Courier> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            if (id == null || body == null) {
                return ResponseEntity.badRequest().build();
            }
            String status = body.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().build();
            }
            Courier updated = courierService.updateStatus(id, status);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("更新小哥状态失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        try {
            return courierService.getStats();
        } catch (Exception e) {
            logger.error("查询小哥统计失败", e);
            return new HashMap<>();
        }
    }
}
