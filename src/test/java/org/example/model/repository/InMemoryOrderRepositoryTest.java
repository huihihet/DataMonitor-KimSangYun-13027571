package org.example.model.repository;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrderRepositoryTest {

    OrderRepository repo = new InMemoryOrderRepository();

    @Test
    void findAllReturnsEightDummyOrders() {
        assertEquals(8, repo.findAll().size());
    }

    @Test
    void findAllIsUnmodifiable() {
        List<Order> list = repo.findAll();
        assertThrows(UnsupportedOperationException.class,
            () -> list.add(new Order(99L, 1L, "테스트", 1, OrderStatus.RESERVED)));
    }

    @Test
    void findByStatusReservedReturnsTwoOrders() {
        assertEquals(2, repo.findByStatus(OrderStatus.RESERVED).size());
    }

    @Test
    void findByStatusProducingReturnsTwoOrders() {
        assertEquals(2, repo.findByStatus(OrderStatus.PRODUCING).size());
    }

    @Test
    void findByStatusConfirmedReturnsTwoOrders() {
        assertEquals(2, repo.findByStatus(OrderStatus.CONFIRMED).size());
    }

    @Test
    void findByStatusReleaseReturnsOneOrder() {
        assertEquals(1, repo.findByStatus(OrderStatus.RELEASE).size());
    }

    @Test
    void findByStatusRejectedReturnsOneOrder() {
        assertEquals(1, repo.findByStatus(OrderStatus.REJECTED).size());
    }

    @Test
    void findBySampleIdOneReturnsThreeOrders() {
        assertEquals(3, repo.findBySampleId(1L).size());
    }

    @Test
    void findBySampleIdTwoReturnsThreeOrders() {
        assertEquals(3, repo.findBySampleId(2L).size());
    }

    @Test
    void findBySampleIdNonExistingReturnsEmpty() {
        assertTrue(repo.findBySampleId(99L).isEmpty());
    }
}
