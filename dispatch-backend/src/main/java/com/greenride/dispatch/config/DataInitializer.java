package com.greenride.dispatch.config;

import com.greenride.dispatch.entity.Bike;
import com.greenride.dispatch.entity.Courier;
import com.greenride.dispatch.repository.BikeRepository;
import com.greenride.dispatch.repository.CourierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;
    private final CourierRepository courierRepository;

    public DataInitializer(BikeRepository bikeRepository, CourierRepository courierRepository) {
        this.bikeRepository = bikeRepository;
        this.courierRepository = courierRepository;
    }

    @Override
    public void run(String... args) {
        if (bikeRepository.count() == 0) {
            initBikes();
        }
        if (courierRepository.count() == 0) {
            initCouriers();
        }
    }

    private void initBikes() {
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

    private void initCouriers() {
        String[][] couriersData = {
            {"C001", "张三", "13800138001", "ON_DUTY"},
            {"C002", "李四", "13800138002", "ON_DUTY"},
            {"C003", "王五", "13800138003", "IDLE"},
            {"C004", "赵六", "13800138004", "ON_DUTY"},
            {"C005", "孙七", "13800138005", "IDLE"},
            {"C006", "周八", "13800138006", "ON_DUTY"},
            {"C007", "吴九", "13800138007", "OFF_DUTY"},
            {"C008", "郑十", "13800138008", "ON_DUTY"}
        };

        double baseLng = 116.4015;
        double baseLat = 39.9140;

        for (int i = 0; i < couriersData.length; i++) {
            String[] data = couriersData[i];
            Courier courier = new Courier();
            courier.setCourierNo(data[0]);
            courier.setName(data[1]);
            courier.setPhone(data[2]);
            courier.setStatus(data[3]);
            courier.setTotalOrders(10 + i * 5);
            courier.setCurrentLng(BigDecimal.valueOf(baseLng + i * 0.0005));
            courier.setCurrentLat(BigDecimal.valueOf(baseLat - i * 0.0003));
            courierRepository.save(courier);
        }

        System.out.println("已初始化 8 名清运小哥数据");
    }
}
