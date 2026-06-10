package com.greenride.dispatch.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class BikeLocationReportDTO {

    @NotBlank(message = "单车编号不能为空")
    @Size(min = 1, max = 32, message = "单车编号长度应在1-32个字符之间")
    private String bikeId;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度不能小于-180")
    @DecimalMax(value = "180.0", message = "经度不能大于180")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
    @DecimalMax(value = "90.0", message = "纬度不能大于90")
    private BigDecimal latitude;

    @DecimalMin(value = "0.0", message = "电量不能小于0")
    @DecimalMax(value = "100.0", message = "电量不能大于100")
    private BigDecimal batteryLevel;

    public BikeLocationReportDTO() {
    }

    public BikeLocationReportDTO(String bikeId, BigDecimal longitude, BigDecimal latitude, BigDecimal batteryLevel) {
        this.bikeId = bikeId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.batteryLevel = batteryLevel;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
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

    public BigDecimal getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(BigDecimal batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
