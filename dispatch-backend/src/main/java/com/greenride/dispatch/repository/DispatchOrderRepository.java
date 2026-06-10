package com.greenride.dispatch.repository;

import com.greenride.dispatch.entity.DispatchOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchOrderRepository extends JpaRepository<DispatchOrder, Long> {

    DispatchOrder findByOrderNo(String orderNo);

    List<DispatchOrder> findByCourierIdAndStatusIn(Long courierId, List<String> statuses);

    List<DispatchOrder> findByStatus(String status);

    Page<DispatchOrder> findByCourierId(Long courierId, Pageable pageable);
}
