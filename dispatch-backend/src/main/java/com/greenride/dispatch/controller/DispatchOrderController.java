package com.greenride.dispatch.controller;

import com.greenride.dispatch.entity.DispatchOrder;
import com.greenride.dispatch.service.DispatchOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dispatch-orders")
public class DispatchOrderController {

    private static final Logger logger = LoggerFactory.getLogger(DispatchOrderController.class);

    private final DispatchOrderService dispatchOrderService;

    public DispatchOrderController(DispatchOrderService dispatchOrderService) {
        this.dispatchOrderService = dispatchOrderService;
    }

    @GetMapping
    public List<DispatchOrder> list(@RequestParam(required = false) String status) {
        try {
            if (status != null && !status.isEmpty()) {
                return dispatchOrderService.findByStatus(status);
            }
            return dispatchOrderService.findAll();
        } catch (Exception e) {
            logger.error("查询调度单列表失败", e);
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispatchOrder> getById(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().build();
            }
            Optional<DispatchOrder> order = dispatchOrderService.findById(id);
            return order.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("查询调度单详情失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/pending")
    public List<DispatchOrder> getPending() {
        try {
            return dispatchOrderService.getPendingOrders();
        } catch (Exception e) {
            logger.error("查询待接单列表失败", e);
            return Collections.emptyList();
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<DispatchOrder> accept(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            if (id == null || body == null) {
                return ResponseEntity.badRequest().build();
            }
            Long courierId = body.get("courierId");
            if (courierId == null) {
                return ResponseEntity.badRequest().build();
            }
            DispatchOrder updated = dispatchOrderService.acceptOrder(id, courierId);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("接单失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<DispatchOrder> start(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            if (id == null || body == null) {
                return ResponseEntity.badRequest().build();
            }
            Long courierId = body.get("courierId");
            if (courierId == null) {
                return ResponseEntity.badRequest().build();
            }
            DispatchOrder updated = dispatchOrderService.startTransport(id, courierId);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("开始运输失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<DispatchOrder> complete(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            if (id == null || body == null) {
                return ResponseEntity.badRequest().build();
            }
            Long courierId = body.get("courierId");
            if (courierId == null) {
                return ResponseEntity.badRequest().build();
            }
            DispatchOrder updated = dispatchOrderService.completeOrder(id, courierId);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("完成订单失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/courier/{courierId}")
    public Page<DispatchOrder> getCourierOrders(@PathVariable Long courierId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        try {
            if (courierId == null) {
                return Page.empty();
            }
            return dispatchOrderService.getCourierOrders(courierId, page, size);
        } catch (Exception e) {
            logger.error("查询小哥订单历史失败 courierId={}", courierId, e);
            return Page.empty();
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        try {
            return dispatchOrderService.getStats();
        } catch (Exception e) {
            logger.error("查询订单统计失败", e);
            return new HashMap<>();
        }
    }
}
