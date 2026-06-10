package com.greenride.dispatch.controller;

import com.greenride.dispatch.dto.TidalAlertRequest;
import com.greenride.dispatch.dto.TidalAlertResponse;
import com.greenride.dispatch.entity.DispatchOrder;
import com.greenride.dispatch.service.CourierService;
import com.greenride.dispatch.service.DispatchOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tidal-alerts")
public class TidalAlertController {

    private static final Logger logger = LoggerFactory.getLogger(TidalAlertController.class);

    private final DispatchOrderService dispatchOrderService;
    private final CourierService courierService;

    public TidalAlertController(DispatchOrderService dispatchOrderService,
                                CourierService courierService) {
        this.dispatchOrderService = dispatchOrderService;
        this.courierService = courierService;
    }

    @PostMapping
    public ResponseEntity<?> receiveAlert(@Valid @RequestBody TidalAlertRequest request) {
        try {
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请求参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            DispatchOrder order = dispatchOrderService.createDispatchOrder(request);

            TidalAlertResponse response = new TidalAlertResponse();
            response.setAlertId(request.getAlertId());

            if (order != null) {
                response.setAccepted(true);
                response.setAssignedCourierName(order.getCourierName());
                response.setDispatchOrderId(order.getId());
                response.setMessage("派单成功");
            } else {
                response.setAccepted(false);
                response.setMessage("暂无空闲小哥，已加入待派单队列");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("处理潮汐预警失败 alertId={}", request != null ? request.getAlertId() : "null", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "处理潮汐预警失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
