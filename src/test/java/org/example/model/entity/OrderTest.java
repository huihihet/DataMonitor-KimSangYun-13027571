package org.example.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void createOrderAllFieldsStored() {
        Order order = new Order(1L, 1L, "서울대 연구실", 50, OrderStatus.RESERVED);
        assertEquals(1L, order.getOrderId());
        assertEquals(1L, order.getSampleId());
        assertEquals("서울대 연구실", order.getCustomerName());
        assertEquals(50, order.getQuantity());
        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    void createOrderNullOrderId() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(null, 1L, "서울대 연구실", 50, OrderStatus.RESERVED));
    }

    @Test
    void createOrderNullSampleId() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(1L, null, "서울대 연구실", 50, OrderStatus.RESERVED));
    }

    @Test
    void createOrderQuantityAtMin() {
        assertDoesNotThrow(() -> new Order(1L, 1L, "서울대 연구실", 1, OrderStatus.RESERVED));
    }

    @Test
    void createOrderQuantityZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(1L, 1L, "서울대 연구실", 0, OrderStatus.RESERVED));
    }

    @Test
    void createOrderNullCustomerName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(1L, 1L, null, 50, OrderStatus.RESERVED));
    }

    @Test
    void createOrderBlankCustomerName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(1L, 1L, "  ", 50, OrderStatus.RESERVED));
    }

    @Test
    void createOrderNullStatus() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(1L, 1L, "서울대 연구실", 50, null));
    }
}
