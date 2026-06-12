package org.example.model.repository;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> store;

    public InMemoryOrderRepository() {
        this.store = new ArrayList<>(List.of(
            new Order(1L, 1L, "서울대 연구실",   50, OrderStatus.RESERVED),
            new Order(2L, 2L, "카이스트 팹리스", 30, OrderStatus.PRODUCING),
            new Order(3L, 3L, "삼성리서치",      20, OrderStatus.CONFIRMED),
            new Order(4L, 1L, "LG이노텍",        10, OrderStatus.RELEASE),
            new Order(5L, 2L, "포스텍",          25, OrderStatus.RESERVED),
            new Order(6L, 3L, "연세대 반도체",   40, OrderStatus.PRODUCING),
            new Order(7L, 1L, "SK하이닉스",      15, OrderStatus.CONFIRMED),
            new Order(8L, 2L, "한양대 연구소",   10, OrderStatus.REJECTED)
        ));
    }

    @Override
    public List<Order> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
    }

    @Override
    public List<Order> findBySampleId(Long sampleId) {
        return store.stream()
                    .filter(o -> o.getSampleId().equals(sampleId))
                    .toList();
    }
}
