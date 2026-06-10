package com.greenride.dispatch.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TidalAlertRequest {

    @NotBlank(message = "预警ID不能为空")
    private String alertId;

    @NotBlank(message = "来源围栏ID不能为空")
    private String sourceFenceId;

    @NotBlank(message = "来源围栏名称不能为空")
    private String sourceFenceName;

    @NotNull(message = "车辆数量不能为空")
    private Integer bikeCount;

    @NotNull(message = "最大容量不能为空")
    private Integer maxCapacity;

    @NotNull(message = "超载率不能为空")
    private Double overloadRatio;

    private String suggestedTargetFenceId;

    private String suggestedTargetFenceName;

    private String timestamp;

    public TidalAlertRequest() {}

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getSourceFenceId() { return sourceFenceId; }
    public void setSourceFenceId(String sourceFenceId) { this.sourceFenceId = sourceFenceId; }

    public String getSourceFenceName() { return sourceFenceName; }
    public void setSourceFenceName(String sourceFenceName) { this.sourceFenceName = sourceFenceName; }

    public Integer getBikeCount() { return bikeCount; }
    public void setBikeCount(Integer bikeCount) { this.bikeCount = bikeCount; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Double getOverloadRatio() { return overloadRatio; }
    public void setOverloadRatio(Double overloadRatio) { this.overloadRatio = overloadRatio; }

    public String getSuggestedTargetFenceId() { return suggestedTargetFenceId; }
    public void setSuggestedTargetFenceId(String suggestedTargetFenceId) { this.suggestedTargetFenceId = suggestedTargetFenceId; }

    public String getSuggestedTargetFenceName() { return suggestedTargetFenceName; }
    public void setSuggestedTargetFenceName(String suggestedTargetFenceName) { this.suggestedTargetFenceName = suggestedTargetFenceName; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
