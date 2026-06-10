package com.greenride.dispatch.dto;

public class GeofenceCheckRequest {

    private String bikeId;

    private PointDTO location;

    public GeofenceCheckRequest() {
    }

    public GeofenceCheckRequest(String bikeId, PointDTO location) {
        this.bikeId = bikeId;
        this.location = location;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public PointDTO getLocation() {
        return location;
    }

    public void setLocation(PointDTO location) {
        this.location = location;
    }

    public static class PointDTO {
        private Double lng;
        private Double lat;

        public PointDTO() {
        }

        public PointDTO(Double lng, Double lat) {
            this.lng = lng;
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }
    }
}
