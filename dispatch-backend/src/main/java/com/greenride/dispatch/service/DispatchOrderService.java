package com.greenride.dispatch.service;

import com.greenride.dispatch.dto.TidalAlertRequest;
import com.greenride.dispatch.entity.Courier;
import com.greenride.dispatch.entity.DispatchOrder;
import com.greenride.dispatch.repository.CourierRepository;
import com.greenride.dispatch.repository.DispatchOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Component
public class DispatchOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchOrderService.class);

    private final DispatchOrderRepository dispatchOrderRepository;
    private final CourierRepository courierRepository;

    public DispatchOrderService(DispatchOrderRepository dispatchOrderRepository,
                                CourierRepository courierRepository) {
        this.dispatchOrderRepository = dispatchOrderRepository;
        this.courierRepository = courierRepository;
    }

    public DispatchOrder createDispatchOrder(TidalAlertRequest alert) {
        try {
            if (alert == null) {
                return null;
            }

            List<Courier> onDutyCouriers = courierRepository.findByStatus("ON_DUTY");
            List<Courier> idleCouriers = courierRepository.findByStatus("IDLE");

            Courier selectedCourier = null;
            if (onDutyCouriers != null && !onDutyCouriers.isEmpty()) {
                selectedCourier = onDutyCouriers.get(0);
            } else if (idleCouriers != null && !idleCouriers.isEmpty()) {
                selectedCourier = idleCouriers.get(0);
            }

            if (selectedCourier == null) {
                return null;
            }

            DispatchOrder order = new DispatchOrder();
            order.setOrderNo(generateOrderNo());
            order.setAlertId(alert.getAlertId());
            order.setCourierId(selectedCourier.getId());
            order.setCourierName(selectedCourier.getName());
            order.setSourceFenceId(alert.getSourceFenceId());
            order.setSourceFenceName(alert.getSourceFenceName());
            order.setTargetFenceId(alert.getSuggestedTargetFenceId());
            order.setTargetFenceName(alert.getSuggestedTargetFenceName());

            Integer bikeCount = alert.getBikeCount();
            Integer maxCapacity = alert.getMaxCapacity();
            int needDispatch = 10;
            if (bikeCount != null && maxCapacity != null) {
                needDispatch = Math.max(bikeCount - maxCapacity, 10);
            }
            order.setBikeCount(needDispatch);

            order.setStatus("PENDING");

            Double overloadRatio = alert.getOverloadRatio();
            String priority = "NORMAL";
            if (overloadRatio != null) {
                if (overloadRatio > 1.5) {
                    priority = "EMERGENCY";
                } else if (overloadRatio > 1.2) {
                    priority = "URGENT";
                }
            }
            order.setPriority(priority);

            DispatchOrder savedOrder = dispatchOrderRepository.save(order);

            selectedCourier.setStatus("ON_DISPATCH");
            selectedCourier.setUpdateTime(LocalDateTime.now());
            courierRepository.save(selectedCourier);

            return savedOrder;
        } catch (Exception e) {
            logger.error("创建调度单失败 alertId={}", alert != null ? alert.getAlertId() : "null", e);
            return null;
        }
    }

    public DispatchOrder acceptOrder(Long orderId, Long courierId) {
        try {
            if (orderId == null || courierId == null) {
                return null;
            }
            Optional<DispatchOrder> orderOpt = dispatchOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return null;
            }
            DispatchOrder order = orderOpt.get();
            if (!"PENDING".equals(order.getStatus())) {
                return null;
            }
            if (!courierId.equals(order.getCourierId())) {
                return null;
            }
            order.setStatus("ACCEPTED");
            order.setAcceptedTime(LocalDateTime.now());
            return dispatchOrderRepository.save(order);
        } catch (Exception e) {
            logger.error("接单失败 orderId={}, courierId={}", orderId, courierId, e);
            return null;
        }
    }

    public DispatchOrder startTransport(Long orderId, Long courierId) {
        try {
            if (orderId == null || courierId == null) {
                return null;
            }
            Optional<DispatchOrder> orderOpt = dispatchOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return null;
            }
            DispatchOrder order = orderOpt.get();
            if (!"ACCEPTED".equals(order.getStatus())) {
                return null;
            }
            if (!courierId.equals(order.getCourierId())) {
                return null;
            }
            order.setStatus("IN_PROGRESS");
            return dispatchOrderRepository.save(order);
        } catch (Exception e) {
            logger.error("开始运输失败 orderId={}, courierId={}", orderId, courierId, e);
            return null;
        }
    }

    public DispatchOrder completeOrder(Long orderId, Long courierId) {
        try {
            if (orderId == null || courierId == null) {
                return null;
            }
            Optional<DispatchOrder> orderOpt = dispatchOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return null;
            }
            DispatchOrder order = orderOpt.get();
            if (!"IN_PROGRESS".equals(order.getStatus())) {
                return null;
            }
            if (!courierId.equals(order.getCourierId())) {
                return null;
            }
            order.setStatus("COMPLETED");
            order.setCompletedTime(LocalDateTime.now());
            DispatchOrder savedOrder = dispatchOrderRepository.save(order);

            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            if (courierOpt.isPresent()) {
                Courier courier = courierOpt.get();
                courier.setStatus("IDLE");
                courier.setUpdateTime(LocalDateTime.now());
                Integer totalOrders = courier.getTotalOrders();
                if (totalOrders == null) {
                    totalOrders = 0;
                }
                courier.setTotalOrders(totalOrders + 1);
                courierRepository.save(courier);
            }

            return savedOrder;
        } catch (Exception e) {
            logger.error("完成订单失败 orderId={}, courierId={}", orderId, courierId, e);
            return null;
        }
    }

    public List<DispatchOrder> findAll() {
        try {
            return dispatchOrderRepository.findAll();
        } catch (Exception e) {
            logger.error("查询所有调度单失败", e);
            return Collections.emptyList();
        }
    }

    public Optional<DispatchOrder> findById(Long id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            return dispatchOrderRepository.findById(id);
        } catch (Exception e) {
            logger.error("根据ID查询调度单失败 id={}", id, e);
            return Optional.empty();
        }
    }

    public List<DispatchOrder> findByStatus(String status) {
        try {
            if (status == null) {
                return Collections.emptyList();
            }
            return dispatchOrderRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("根据状态查询调度单失败 status={}", status, e);
            return Collections.emptyList();
        }
    }

    public List<DispatchOrder> getPendingOrders() {
        try {
            return dispatchOrderRepository.findByStatus("PENDING");
        } catch (Exception e) {
            logger.error("查询待接单列表失败", e);
            return Collections.emptyList();
        }
    }

    public Page<DispatchOrder> getCourierOrders(Long courierId, int page, int size) {
        try {
            if (courierId == null) {
                return Page.empty();
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            return dispatchOrderRepository.findByCourierId(courierId, pageable);
        } catch (Exception e) {
            logger.error("查询小哥订单历史失败 courierId={}", courierId, e);
            return Page.empty();
        }
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<DispatchOrder> allOrders = dispatchOrderRepository.findAll();
            long pending = 0;
            long accepted = 0;
            long inProgress = 0;
            long completed = 0;
            long emergency = 0;
            long urgent = 0;
            long normal = 0;

            if (allOrders != null) {
                for (DispatchOrder order : allOrders) {
                    String status = order.getStatus();
                    if ("PENDING".equals(status)) {
                        pending++;
                    } else if ("ACCEPTED".equals(status)) {
                        accepted++;
                    } else if ("IN_PROGRESS".equals(status)) {
                        inProgress++;
                    } else if ("COMPLETED".equals(status)) {
                        completed++;
                    }
                    String priority = order.getPriority();
                    if ("EMERGENCY".equals(priority)) {
                        emergency++;
                    } else if ("URGENT".equals(priority)) {
                        urgent++;
                    } else if ("NORMAL".equals(priority)) {
                        normal++;
                    }
                }
            }

            Map<String, Long> statusStats = new HashMap<>();
            statusStats.put("PENDING", pending);
            statusStats.put("ACCEPTED", accepted);
            statusStats.put("IN_PROGRESS", inProgress);
            statusStats.put("COMPLETED", completed);
            stats.put("status", statusStats);

            Map<String, Long> priorityStats = new HashMap<>();
            priorityStats.put("EMERGENCY", emergency);
            priorityStats.put("URGENT", urgent);
            priorityStats.put("NORMAL", normal);
            stats.put("priority", priorityStats);

            stats.put("total", allOrders != null ? (long) allOrders.size() : 0L);
        } catch (Exception e) {
            logger.error("统计订单状态失败", e);
        }
        return stats;
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "DO" + datePart + sb.toString();
    }
}
