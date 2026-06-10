package com.greenride.dispatch.repository;

import com.greenride.dispatch.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

    Courier findByCourierNo(String courierNo);

    List<Courier> findByStatus(String status);

    long countByStatus(String status);
}
