package org.example;

import org.example.app.MonitoringLoop;
import org.example.controller.MonitoringController;
import org.example.model.repository.InMemoryOrderRepository;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.OrderRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.MonitoringView;

public class Main {
    public static void main(String[] args) {
        SampleRepository     sampleRepo = new InMemorySampleRepository();
        OrderRepository      orderRepo  = new InMemoryOrderRepository();
        MonitoringView       view       = new MonitoringView();
        MonitoringController controller = new MonitoringController(sampleRepo, orderRepo, view);
        MonitoringLoop       loop       = new MonitoringLoop(controller);
        loop.start();
    }
}
