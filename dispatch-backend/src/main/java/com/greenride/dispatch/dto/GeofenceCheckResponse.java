package com.greenride.dispatch.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceCheckResponse {

    private String bikeId;

    private Boolean isInside;

    private FenceInfo insideFence;

    private FenceInfo nearestFence;

    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FenceInfo {
        private String fenceId;
        private String name;
        private String type;
        private Double distanceToEdge;
    }
}
