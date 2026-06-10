package com.greenride.dispatch.dto;

public class GeofenceCheckResponse {

    private String bikeId;

    private Boolean isInside;

    private FenceInfo insideFence;

    private FenceInfo nearestFence;

    private String message;

    public GeofenceCheckResponse() {
    }

    public GeofenceCheckResponse(String bikeId, Boolean isInside, FenceInfo insideFence, FenceInfo nearestFence, String message) {
        this.bikeId = bikeId;
        this.isInside = isInside;
        this.insideFence = insideFence;
        this.nearestFence = nearestFence;
        this.message = message;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public Boolean getIsInside() {
        return isInside;
    }

    public void setIsInside(Boolean isInside) {
        this.isInside = isInside;
    }

    public FenceInfo getInsideFence() {
        return insideFence;
    }

    public void setInsideFence(FenceInfo insideFence) {
        this.insideFence = insideFence;
    }

    public FenceInfo getNearestFence() {
        return nearestFence;
    }

    public void setNearestFence(FenceInfo nearestFence) {
        this.nearestFence = nearestFence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class FenceInfo {
        private String fenceId;
        private String name;
        private String type;
        private Double distanceToEdge;

        public FenceInfo() {
        }

        public FenceInfo(String fenceId, String name, String type, Double distanceToEdge) {
            this.fenceId = fenceId;
            this.name = name;
            this.type = type;
            this.distanceToEdge = distanceToEdge;
        }

        public String getFenceId() {
            return fenceId;
        }

        public void setFenceId(String fenceId) {
            this.fenceId = fenceId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getDistanceToEdge() {
            return distanceToEdge;
        }

        public void setDistanceToEdge(Double distanceToEdge) {
            this.distanceToEdge = distanceToEdge;
        }
    }
}
