package com.greenride.dispatch.config;

import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.repository.BikeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;

    public DataInitializer(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }

    @Override
    public void run(String... args) {
        if (bikeRepository.count() > 0) {
            return;
        }

        String[] bikeTypes = {"STANDARD", "ELECTRIC", "MOUNTAIN"};
        String[] statuses = {"AVAILABLE", "IN_USE", "MAINTENANCE"};

        double baseLng = 116.4000;
        double baseLat = 39.9130;

        for (int i = 1; i <= 2000; i++) {
            Bike bike = new Bike();
            bike.setBikeId("B" + String.format("%05d", i));
            bike.setBikeType(bikeTypes[i % bikeTypes.length]);
            bike.setStatus(statuses[i % statuses.length]);
            bike.setBatteryLevel(BigDecimal.valueOf(50 + (i % 50)));

            double lng = baseLng + (i % 50) * 0.0003;
            double lat = baseLat + (i / 50) * 0.0002;
            bike.setLongitude(BigDecimal.valueOf(lng));
            bike.setLatitude(BigDecimal.valueOf(lat));

            bike.setLastReportTime(LocalDateTime.now().minusMinutes(i % 60));
            bikeRepository.save(bike);
        }

        System.out.println("已初始化 2000 辆单车基础台账数据");
    }
}
