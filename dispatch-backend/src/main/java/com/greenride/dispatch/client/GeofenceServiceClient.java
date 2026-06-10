package com.greenride.dispatch.client;

import com.greenride.dispatch.dto.GeofenceCheckRequest;
import com.greenride.dispatch.dto.GeofenceCheckResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeofenceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${geofence.service.url}")
    private String geofenceServiceUrl;

    public GeofenceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeofenceCheckResponse checkGeofence(String bikeId, Double lng, Double lat) {
        GeofenceCheckRequest request = new GeofenceCheckRequest();
        request.setBikeId(bikeId);
        GeofenceCheckRequest.PointDTO point = new GeofenceCheckRequest.PointDTO(lng, lat);
        request.setLocation(point);

        String url = geofenceServiceUrl + "/check";
        return restTemplate.postForObject(url, request, GeofenceCheckResponse.class);
    }
}
