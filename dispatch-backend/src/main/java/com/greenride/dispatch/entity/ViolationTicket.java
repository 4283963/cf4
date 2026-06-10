package com.greenride.dispatch.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public ViolationTicket() {
    }

    public ViolationTicket(Long id, String ticketNo, String bikeId, String violationType, BigDecimal longitude, BigDecimal latitude, BigDecimal fineAmount, String status, String nearestFenceId, String nearestFenceName, BigDecimal distanceToFence, String description, String userId, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.ticketNo = ticketNo;
        this.bikeId = bikeId;
        this.violationType = violationType;
        this.longitude = longitude;
        this.latitude = latitude;
        this.fineAmount = fineAmount;
        this.status = status;
        this.nearestFenceId = nearestFenceId;
        this.nearestFenceName = nearestFenceName;
        this.distanceToFence = distanceToFence;
        this.description = description;
        this.userId = userId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNearestFenceId() {
        return nearestFenceId;
    }

    public void setNearestFenceId(String nearestFenceId) {
        this.nearestFenceId = nearestFenceId;
    }

    public String getNearestFenceName() {
        return nearestFenceName;
    }

    public void setNearestFenceName(String nearestFenceName) {
        this.nearestFenceName = nearestFenceName;
    }

    public BigDecimal getDistanceToFence() {
        return distanceToFence;
    }

    public void setDistanceToFence(BigDecimal distanceToFence) {
        this.distanceToFence = distanceToFence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

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
