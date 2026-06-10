package com.greenride.dispatch.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BikeLocationReportDTO {

    private String bikeId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal batteryLevel;
}
