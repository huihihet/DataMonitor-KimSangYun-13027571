package org.example.controller;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import org.example.model.entity.Sample;
import org.example.model.entity.SampleStatus;
import org.example.model.repository.OrderRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.MonitoringView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonitoringController {
    private final SampleRepository sampleRepo;
    private final OrderRepository orderRepo;
    private final MonitoringView view;

    public MonitoringController(SampleRepository sampleRepo,
                                OrderRepository orderRepo,
                                MonitoringView view) {
        this.sampleRepo = sampleRepo;
        this.orderRepo = orderRepo;
        this.view = view;
    }

    public void refresh() {
        List<Order> orders = orderRepo.findAll();

        Map<OrderStatus, Long> statusCounts = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.REJECTED)
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        List<Sample> samples = sampleRepo.findAll();

        List<SampleStatus> sampleStatusList = new ArrayList<>();
        for (Sample sample : samples) {
            int demandSum = orders.stream()
                    .filter(o -> o.getSampleId().equals(sample.getSampleId())
                            && (o.getStatus() == OrderStatus.RESERVED || o.getStatus() == OrderStatus.PRODUCING))
                    .mapToInt(Order::getQuantity)
                    .sum();
            String stockLevel = calcStockLevel(sample.getStock(), demandSum);
            sampleStatusList.add(new SampleStatus(sample.getSampleId(), sample.getName(), sample.getStock(), stockLevel));
        }

        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        view.render(statusCounts, sampleStatusList, timestamp);
    }

    public void shutdown() {
        view.printShutdownMessage();
    }

    private String calcStockLevel(int stock, int demandSum) {
        if (stock == 0) return "고갈";
        if (stock < demandSum) return "부족";
        return "여유";
    }
}
