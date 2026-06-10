package com.greenride.dispatch.repository;

import com.greenride.dispatch.entity.ViolationTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ViolationTicketRepository extends JpaRepository<ViolationTicket, Long> {

    ViolationTicket findByTicketNo(String ticketNo);

    Page<ViolationTicket> findByBikeId(String bikeId, Pageable pageable);

    Page<ViolationTicket> findByStatus(String status, Pageable pageable);

    List<ViolationTicket> findByBikeIdAndCreateTimeAfter(String bikeId, LocalDateTime time);

    long countByStatus(String status);
}
