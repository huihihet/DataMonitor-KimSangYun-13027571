package org.example.view;

import org.example.model.entity.OrderStatus;
import org.example.model.entity.SampleStatus;

import java.util.List;
import java.util.Map;

public class MonitoringView {
    private static final String SEPARATOR    = "=".repeat(40);
    private static final String ANSI_RESET   = "\033[0m";
    private static final String ANSI_YELLOW  = "\033[33m";
    private static final String ANSI_RED     = "\033[31m";

    public void render(Map<OrderStatus, Long> statusCounts,
                       List<SampleStatus> sampleStatuses,
                       String timestamp) {
        clearScreen();
        printHeader(timestamp);
        printOrderSummary(statusCounts);
        printInventory(sampleStatuses);
        printFooter(3);
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printHeader(String timestamp) {
        System.out.println(SEPARATOR);
        System.out.println("  DataMonitor — S-Semi 실시간 모니터링");
        System.out.println("  마지막 갱신: " + timestamp + "  (q: 종료)");
        System.out.println(SEPARATOR);
    }

    public void printOrderSummary(Map<OrderStatus, Long> statusCounts) {
        System.out.println();
        System.out.println("[주문 현황]");
        System.out.printf("  %-10s: %4d건%n", "RESERVED",  statusCounts.getOrDefault(OrderStatus.RESERVED,  0L));
        System.out.printf("  %-10s: %4d건%n", "PRODUCING", statusCounts.getOrDefault(OrderStatus.PRODUCING, 0L));
        System.out.printf("  %-10s: %4d건%n", "CONFIRMED", statusCounts.getOrDefault(OrderStatus.CONFIRMED, 0L));
        System.out.printf("  %-10s: %4d건%n", "RELEASE",   statusCounts.getOrDefault(OrderStatus.RELEASE,   0L));
    }

    public void printInventory(List<SampleStatus> sampleStatuses) {
        System.out.println();
        System.out.println("[시료별 재고 현황]");
        System.out.printf("  %4s  %-12s  %7s  %s%n", "ID", "이름", "재고", "상태");
        System.out.printf("  %4s  %-12s  %7s  %s%n", "--", "----------", "-------", "----");
        for (SampleStatus s : sampleStatuses) {
            String coloredLevel = colorize(s.stockLevel());
            System.out.printf("  %4d  %-12s  %7d  %s%n", s.sampleId(), s.name(), s.stock(), coloredLevel);
        }
    }

    public void printFooter(int intervalSeconds) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  다음 갱신까지 " + intervalSeconds + "초...");
        System.out.println(SEPARATOR);
    }

    public void printShutdownMessage() {
        System.out.println("모니터링을 종료합니다.");
    }

    private String colorize(String stockLevel) {
        return switch (stockLevel) {
            case "부족" -> ANSI_YELLOW + "부족" + ANSI_RESET;
            case "고갈" -> ANSI_RED    + "고갈" + ANSI_RESET;
            default     -> stockLevel;
        };
    }
}
