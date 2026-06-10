package com.greenride.dispatch.service;

import com.greenride.dispatch.entity.Courier;
import com.greenride.dispatch.repository.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CourierService {

    private static final Logger logger = LoggerFactory.getLogger(CourierService.class);

    private final CourierRepository courierRepository;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public List<Courier> findAll() {
        try {
            return courierRepository.findAll();
        } catch (Exception e) {
            logger.error("查询所有小哥失败", e);
            return Collections.emptyList();
        }
    }

    public Optional<Courier> findById(Long id) {
        try {
            if (id == null) {
                return Optional.empty();
            }
            return courierRepository.findById(id);
        } catch (Exception e) {
            logger.error("根据ID查询小哥失败 id={}", id, e);
            return Optional.empty();
        }
    }

    public Courier findByCourierNo(String courierNo) {
        try {
            if (courierNo == null) {
                return null;
            }
            return courierRepository.findByCourierNo(courierNo);
        } catch (Exception e) {
            logger.error("根据编号查询小哥失败 courierNo={}", courierNo, e);
            return null;
        }
    }

    public List<Courier> findByStatus(String status) {
        try {
            if (status == null) {
                return Collections.emptyList();
            }
            return courierRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("根据状态查询小哥失败 status={}", status, e);
            return Collections.emptyList();
        }
    }

    public Courier save(Courier courier) {
        try {
            if (courier == null) {
                return null;
            }
            return courierRepository.save(courier);
        } catch (Exception e) {
            logger.error("保存小哥信息失败", e);
            return null;
        }
    }

    public Courier updateStatus(Long id, String status) {
        try {
            if (id == null || status == null) {
                return null;
            }
            Optional<Courier> courierOpt = courierRepository.findById(id);
            if (!courierOpt.isPresent()) {
                return null;
            }
            Courier courier = courierOpt.get();
            courier.setStatus(status);
            courier.setUpdateTime(LocalDateTime.now());
            return courierRepository.save(courier);
        } catch (Exception e) {
            logger.error("更新小哥状态失败 id={}, status={}", id, status, e);
            return null;
        }
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        try {
            stats.put("ON_DUTY", courierRepository.countByStatus("ON_DUTY"));
            stats.put("IDLE", courierRepository.countByStatus("IDLE"));
            stats.put("ON_DISPATCH", courierRepository.countByStatus("ON_DISPATCH"));
            stats.put("OFF_DUTY", courierRepository.countByStatus("OFF_DUTY"));
        } catch (Exception e) {
            logger.error("统计小哥状态失败", e);
        }
        return stats;
    }
}
