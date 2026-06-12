package org.example.model.entity;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void hasAllFiveValues() {
        assertEquals(5, OrderStatus.values().length);
    }

    @Test
    void containsExpectedConstants() {
        EnumSet<OrderStatus> expected = EnumSet.of(
            OrderStatus.RESERVED,
            OrderStatus.PRODUCING,
            OrderStatus.CONFIRMED,
            OrderStatus.RELEASE,
            OrderStatus.REJECTED
        );
        EnumSet<OrderStatus> actual = EnumSet.allOf(OrderStatus.class);
        assertEquals(expected, actual);
    }
}
