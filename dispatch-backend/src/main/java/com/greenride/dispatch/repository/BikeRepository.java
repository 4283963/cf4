package com.greenride.dispatch.repository;

import com.greenride.dispatch.entity.Bike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikeRepository extends JpaRepository<Bike, String> {

    List<Bike> findByStatus(String status);

    Page<Bike> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
