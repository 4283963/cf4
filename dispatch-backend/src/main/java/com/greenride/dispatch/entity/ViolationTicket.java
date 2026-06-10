package com.greenride.dispatch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "violation_ticket")
public class ViolationTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", length = 32, unique = true)
    private String ticketNo;

    @Column(name = "bike_id", length = 32)
    private String bikeId;

    @Column(name = "violation_type", length = 50)
    private String violationType;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "nearest_fence_id", length = 32)
    private String nearestFenceId;

    @Column(name = "nearest_fence_name", length = 100)
    private String nearestFenceName;

    @Column(name = "distance_to_fence", precision = 10, scale = 4)
    private BigDecimal distanceToFence;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "user_id", length = 32)
    private String userId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
