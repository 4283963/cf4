package com.greenride.dispatch.controller;

import com.greenride.dispatch.entity.ViolationTicket;
import com.greenride.dispatch.service.ViolationTicketService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/violation-tickets")
public class ViolationTicketController {

    private final ViolationTicketService violationTicketService;

    public ViolationTicketController(ViolationTicketService violationTicketService) {
        this.violationTicketService = violationTicketService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        Page<ViolationTicket> ticketPage = violationTicketService.listTickets(page, size, status);

        Map<String, Object> result = new HashMap<>();
        result.put("total", ticketPage.getTotalElements());
        result.put("page", ticketPage.getNumber());
        result.put("size", ticketPage.getSize());
        result.put("tickets", ticketPage.getContent());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViolationTicket> getTicketById(@PathVariable Long id) {
        return violationTicketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{ticketNo}")
    public ResponseEntity<ViolationTicket> getTicketByNo(@PathVariable String ticketNo) {
        ViolationTicket ticket = violationTicketService.getTicketByNo(ticketNo);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ViolationTicket> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        ViolationTicket ticket = violationTicketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("unpaidCount", violationTicketService.countByStatus("UNPAID"));
        stats.put("paidCount", violationTicketService.countByStatus("PAID"));
        stats.put("appealedCount", violationTicketService.countByStatus("APPEALED"));
        return ResponseEntity.ok(stats);
    }
}
