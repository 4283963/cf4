package com.greenride.dispatch.service;

import com.greenride.dispatch.dto.BikeLocationReportDTO;
import com.greenride.dispatch.dto.GeofenceCheckResponse;
import com.greenride.dispatch.entity.ViolationTicket;
import com.greenride.dispatch.repository.ViolationTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ViolationTicketService {

    private final ViolationTicketRepository violationTicketRepository;

    private static final BigDecimal DEFAULT_FINE = new BigDecimal("5.00");

    public ViolationTicketService(ViolationTicketRepository violationTicketRepository) {
        this.violationTicketRepository = violationTicketRepository;
    }

    @Transactional
    public ViolationTicket createViolationTicket(BikeLocationReportDTO dto, GeofenceCheckResponse fenceResult) {
        ViolationTicket ticket = new ViolationTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setBikeId(dto.getBikeId());
        ticket.setViolationType("ILLEGAL_PARKING");
        ticket.setLongitude(dto.getLongitude());
        ticket.setLatitude(dto.getLatitude());
        ticket.setFineAmount(DEFAULT_FINE);
        ticket.setStatus("UNPAID");

        if (fenceResult.getNearestFence() != null) {
            ticket.setNearestFenceId(fenceResult.getNearestFence().getFenceId());
            ticket.setNearestFenceName(fenceResult.getNearestFence().getName());
            ticket.setDistanceToFence(BigDecimal.valueOf(fenceResult.getNearestFence().getDistanceToEdge()));
        }

        ticket.setDescription(fenceResult.getMessage());

        return violationTicketRepository.save(ticket);
    }

    public Page<ViolationTicket> listTickets(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        if (status != null && !status.isEmpty()) {
            return violationTicketRepository.findByStatus(status, pageable);
        }
        return violationTicketRepository.findAll(pageable);
    }

    public Optional<ViolationTicket> getTicketById(Long id) {
        return violationTicketRepository.findById(id);
    }

    public ViolationTicket getTicketByNo(String ticketNo) {
        return violationTicketRepository.findByTicketNo(ticketNo);
    }

    @Transactional
    public ViolationTicket updateTicketStatus(Long id, String status) {
        ViolationTicket ticket = violationTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("罚单不存在"));
        ticket.setStatus(status);
        return violationTicketRepository.save(ticket);
    }

    public long countByStatus(String status) {
        return violationTicketRepository.countByStatus(status);
    }

    private String generateTicketNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "VT" + dateStr + uuid;
    }
}
