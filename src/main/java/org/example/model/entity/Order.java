package org.example.model.entity;

public class Order {
    private final Long orderId;
    private final Long sampleId;
    private final String customerName;
    private final int quantity;
    private final OrderStatus status;

    public Order(Long orderId, Long sampleId, String customerName, int quantity, OrderStatus status) {
        if (orderId == null) throw new IllegalArgumentException("주문 ID는 null일 수 없습니다.");
        if (sampleId == null) throw new IllegalArgumentException("시료 ID는 null일 수 없습니다.");
        if (customerName == null || customerName.isBlank()) throw new IllegalArgumentException("고객명은 공백일 수 없습니다.");
        if (quantity < 1) throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        if (status == null) throw new IllegalArgumentException("주문 상태는 null일 수 없습니다.");
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getOrderId() { return orderId; }
    public Long getSampleId() { return sampleId; }
    public String getCustomerName() { return customerName; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
}
