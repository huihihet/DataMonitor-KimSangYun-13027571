package org.example.model.repository;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import java.util.List;

public interface OrderRepository {
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySampleId(Long sampleId);
}
