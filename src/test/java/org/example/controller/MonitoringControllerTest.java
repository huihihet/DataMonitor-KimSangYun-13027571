package org.example.controller;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import org.example.model.entity.Sample;
import org.example.model.entity.SampleStatus;
import org.example.model.repository.InMemoryOrderRepository;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.OrderRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.MonitoringView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringControllerTest {

    private static class SpyMonitoringView extends MonitoringView {
        Map<OrderStatus, Long> capturedStatusCounts;
        List<SampleStatus> capturedSampleStatuses;
        String capturedTimestamp;
        int renderCallCount = 0;

        @Override
        public void render(Map<OrderStatus, Long> statusCounts,
                           List<SampleStatus> sampleStatuses,
                           String timestamp) {
            this.capturedStatusCounts   = statusCounts;
            this.capturedSampleStatuses = sampleStatuses;
            this.capturedTimestamp      = timestamp;
            this.renderCallCount++;
        }

        @Override
        public void clearScreen() {}
    }

    private static class StubSampleRepository implements SampleRepository {
        private final List<Sample> samples;

        StubSampleRepository(Sample... samples) {
            this.samples = List.of(samples);
        }

        @Override
        public List<Sample> findAll() { return samples; }

        @Override
        public Optional<Sample> findById(Long id) { return Optional.empty(); }
    }

    private static class StubOrderRepository implements OrderRepository {
        private final List<Order> orders;

        StubOrderRepository(Order... orders) {
            this.orders = List.of(orders);
        }

        @Override
        public List<Order> findAll() { return orders; }

        @Override
        public List<Order> findByStatus(OrderStatus s) { return List.of(); }

        @Override
        public List<Order> findBySampleId(Long id) { return List.of(); }
    }

    SpyMonitoringView    spyView    = new SpyMonitoringView();
    SampleRepository     sampleRepo = new InMemorySampleRepository();
    OrderRepository      orderRepo  = new InMemoryOrderRepository();
    MonitoringController controller = new MonitoringController(sampleRepo, orderRepo, spyView);

    @BeforeEach
    void setUp() {
        spyView = new SpyMonitoringView();
        sampleRepo = new InMemorySampleRepository();
        orderRepo  = new InMemoryOrderRepository();
        controller = new MonitoringController(sampleRepo, orderRepo, spyView);
    }

    @Test
    void refreshCallsViewRenderOnce() {
        controller.refresh();
        assertEquals(1, spyView.renderCallCount);
    }

    @Test
    void refreshStatusCountsReservedTwo() {
        controller.refresh();
        assertEquals(2L, spyView.capturedStatusCounts.get(OrderStatus.RESERVED));
    }

    @Test
    void refreshStatusCountsProducingTwo() {
        controller.refresh();
        assertEquals(2L, spyView.capturedStatusCounts.get(OrderStatus.PRODUCING));
    }

    @Test
    void refreshStatusCountsConfirmedTwo() {
        controller.refresh();
        assertEquals(2L, spyView.capturedStatusCounts.get(OrderStatus.CONFIRMED));
    }

    @Test
    void refreshStatusCountsReleaseOne() {
        controller.refresh();
        assertEquals(1L, spyView.capturedStatusCounts.get(OrderStatus.RELEASE));
    }

    @Test
    void refreshRejectedExcludedFromCounts() {
        controller.refresh();
        assertFalse(spyView.capturedStatusCounts.containsKey(OrderStatus.REJECTED));
    }

    @Test
    void refreshAlphaChipStockLevelYeoyu() {
        controller.refresh();
        SampleStatus alphaChip = spyView.capturedSampleStatuses.stream()
                .filter(s -> s.sampleId() == 1L)
                .findFirst()
                .orElseThrow();
        assertEquals("여유", alphaChip.stockLevel());
    }

    @Test
    void refreshBetaWaferStockLevelGogal() {
        controller.refresh();
        SampleStatus betaWafer = spyView.capturedSampleStatuses.stream()
                .filter(s -> s.sampleId() == 2L)
                .findFirst()
                .orElseThrow();
        assertEquals("고갈", betaWafer.stockLevel());
    }

    @Test
    void refreshGammaCoreStockLevelBuchok() {
        controller.refresh();
        SampleStatus gammaCore = spyView.capturedSampleStatuses.stream()
                .filter(s -> s.sampleId() == 3L)
                .findFirst()
                .orElseThrow();
        assertEquals("부족", gammaCore.stockLevel());
    }

    @Test
    void refreshTimestampFormat() {
        controller.refresh();
        assertTrue(spyView.capturedTimestamp.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    // --- 경계값 케이스 ---

    @Test
    void refreshStockEqualsDemandIsYeoyu() {
        // stock=50, demand=50 (RESERVED qty=50) → stock >= demand → "여유"
        SampleRepository stubSample = new StubSampleRepository(
                new Sample(1L, "TestChip", 10, 0.9, 50)
        );
        OrderRepository stubOrder = new StubOrderRepository(
                new Order(1L, 1L, "고객A", 50, OrderStatus.RESERVED)
        );
        MonitoringController c = new MonitoringController(stubSample, stubOrder, spyView);
        c.refresh();

        SampleStatus status = spyView.capturedSampleStatuses.get(0);
        assertEquals("여유", status.stockLevel());
    }

    @Test
    void refreshDemandZeroStockPositiveIsYeoyu() {
        // RESERVED·PRODUCING 주문 없는 시료, stock=10 → demand=0 → "여유"
        SampleRepository stubSample = new StubSampleRepository(
                new Sample(1L, "TestChip", 10, 0.9, 10)
        );
        OrderRepository stubOrder = new StubOrderRepository(
                new Order(1L, 1L, "고객A", 5, OrderStatus.CONFIRMED)
        );
        MonitoringController c = new MonitoringController(stubSample, stubOrder, spyView);
        c.refresh();

        SampleStatus status = spyView.capturedSampleStatuses.get(0);
        assertEquals("여유", status.stockLevel());
    }

    @Test
    void refreshStockZeroDemandZeroIsGogal() {
        // stock=0, demand=0 → stock==0 우선 판별 → "고갈"
        SampleRepository stubSample = new StubSampleRepository(
                new Sample(1L, "TestChip", 10, 0.9, 0)
        );
        OrderRepository stubOrder = new StubOrderRepository();
        MonitoringController c = new MonitoringController(stubSample, stubOrder, spyView);
        c.refresh();

        SampleStatus status = spyView.capturedSampleStatuses.get(0);
        assertEquals("고갈", status.stockLevel());
    }

    @Test
    void refreshConfirmedReleasedExcludedFromDemand() {
        // CONFIRMED·RELEASE 주문만 있는 시료, stock=5 → demand=0 → "여유"
        SampleRepository stubSample = new StubSampleRepository(
                new Sample(1L, "TestChip", 10, 0.9, 5)
        );
        OrderRepository stubOrder = new StubOrderRepository(
                new Order(1L, 1L, "고객A", 100, OrderStatus.CONFIRMED),
                new Order(2L, 1L, "고객B", 200, OrderStatus.RELEASE)
        );
        MonitoringController c = new MonitoringController(stubSample, stubOrder, spyView);
        c.refresh();

        SampleStatus status = spyView.capturedSampleStatuses.get(0);
        assertEquals("여유", status.stockLevel());
    }
}
