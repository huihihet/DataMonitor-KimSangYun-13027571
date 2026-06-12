package org.example.view;

import org.example.model.entity.OrderStatus;
import org.example.model.entity.SampleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitoringViewTest {
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final MonitoringView view = new MonitoringView();

    private final Map<OrderStatus, Long> statusCounts = Map.of(
            OrderStatus.RESERVED,  2L,
            OrderStatus.PRODUCING, 2L,
            OrderStatus.CONFIRMED, 2L,
            OrderStatus.RELEASE,   1L
    );

    private final List<SampleStatus> sampleStatuses = List.of(
            new SampleStatus(1L, "AlphaChip", 120, "여유"),
            new SampleStatus(2L, "BetaWafer",   0, "고갈"),
            new SampleStatus(3L, "GammaCore",  15, "부족")
    );

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    void renderContainsSeparator() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("========"));
    }

    @Test
    void renderContainsTitle() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("DataMonitor"));
    }

    @Test
    void renderContainsTimestamp() {
        view.printHeader("14:32:05");
        assertTrue(outContent.toString().contains("14:32:05"));
    }

    @Test
    void renderContainsQuitHint() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("(q: 종료)"));
    }

    @Test
    void renderContainsOrderSummaryHeader() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("[주문 현황]"));
    }

    @Test
    void renderContainsReservedCount() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        String output = outContent.toString();
        assertTrue(output.contains("RESERVED") && output.contains("2"));
    }

    @Test
    void renderContainsInventoryHeader() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("[시료별 재고 현황]"));
    }

    @Test
    void renderContainsYeoyu() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("여유"));
    }

    @Test
    void renderContainsBuchok() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        String output = outContent.toString();
        assertTrue(output.contains("\033[33m") && output.contains("부족"));
    }

    @Test
    void renderContainsGogal() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        String output = outContent.toString();
        assertTrue(output.contains("\033[31m") && output.contains("고갈"));
    }

    @Test
    void renderContainsFooter() {
        view.render(statusCounts, sampleStatuses, "14:32:05");
        assertTrue(outContent.toString().contains("다음 갱신까지"));
    }

    @Test
    void printShutdownMessageContainsText() {
        view.printShutdownMessage();
        assertTrue(outContent.toString().contains("모니터링을 종료합니다."));
    }
}
