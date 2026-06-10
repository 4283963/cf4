package com.greenride.dispatch.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceCheckRequest {

    private String bikeId;

    private PointDTO location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointDTO {
        private Double lng;
        private Double lat;
    }
}
