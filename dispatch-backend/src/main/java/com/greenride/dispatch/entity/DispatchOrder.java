package com.greenride.dispatch.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_order")
public class DispatchOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, unique = true)
    private String orderNo;

    @Column(length = 64)
    private String alertId;

    private Long courierId;

    private String courierName;

    private String sourceFenceId;

    private String sourceFenceName;

    private String targetFenceId;

    private String targetFenceName;

    private Integer bikeCount;

    @Column(length = 20)
    private String status;

    @Column(length = 20)
    private String priority;

    @Column(length = 500)
    private String remark;

    private LocalDateTime acceptedTime;

    private LocalDateTime completedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public DispatchOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getSourceFenceId() { return sourceFenceId; }
    public void setSourceFenceId(String sourceFenceId) { this.sourceFenceId = sourceFenceId; }

    public String getSourceFenceName() { return sourceFenceName; }
    public void setSourceFenceName(String sourceFenceName) { this.sourceFenceName = sourceFenceName; }

    public String getTargetFenceId() { return targetFenceId; }
    public void setTargetFenceId(String targetFenceId) { this.targetFenceId = targetFenceId; }

    public String getTargetFenceName() { return targetFenceName; }
    public void setTargetFenceName(String targetFenceName) { this.targetFenceName = targetFenceName; }

    public Integer getBikeCount() { return bikeCount; }
    public void setBikeCount(Integer bikeCount) { this.bikeCount = bikeCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getAcceptedTime() { return acceptedTime; }
    public void setAcceptedTime(LocalDateTime acceptedTime) { this.acceptedTime = acceptedTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

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
