package org.example.integration;

import org.example.controller.MonitoringController;
import org.example.model.repository.InMemoryOrderRepository;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.OrderRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.MonitoringView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitoringIntegrationTest {
    private final ByteArrayOutputStream outContent  = new ByteArrayOutputStream();
    private final PrintStream           originalOut = System.out;

    private SampleRepository     sampleRepo;
    private OrderRepository      orderRepo;
    private MonitoringView       view;
    private MonitoringController controller;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        sampleRepo = new InMemorySampleRepository();
        orderRepo  = new InMemoryOrderRepository();
        view       = new MonitoringView();
        controller = new MonitoringController(sampleRepo, orderRepo, view);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    void refreshRendersOnce() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(output.contains("DataMonitor"), "출력에 'DataMonitor' 포함 여부");
    }

    @Test
    void refreshStatusCountsMatchDummyData() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(output.contains("RESERVED"),  "출력에 'RESERVED' 포함 여부");
        assertTrue(output.contains("PRODUCING"), "출력에 'PRODUCING' 포함 여부");
        assertTrue(output.contains("CONFIRMED"), "출력에 'CONFIRMED' 포함 여부");
        assertTrue(output.contains("RELEASE"),   "출력에 'RELEASE' 포함 여부");
    }

    @Test
    void refreshRejectedExcluded() {
        controller.refresh();
        String output = outContent.toString();
        assertFalse(output.contains("REJECTED"), "출력에 'REJECTED' 미포함 여부");
    }

    @Test
    void refreshAlphaChipYeoyu() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(output.contains("AlphaChip"), "출력에 'AlphaChip' 포함 여부");
        assertTrue(output.contains("여유"),       "출력에 '여유' 포함 여부");
    }

    @Test
    void refreshBetaWaferGogal() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(output.contains("BetaWafer"),  "출력에 'BetaWafer' 포함 여부");
        assertTrue(output.contains("\033[31m"),    "출력에 ANSI 적색 코드 포함 여부");
    }

    @Test
    void refreshGammaCorePartial() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(output.contains("GammaCore"),  "출력에 'GammaCore' 포함 여부");
        assertTrue(output.contains("\033[33m"),    "출력에 ANSI 황색 코드 포함 여부");
    }

    @Test
    void refreshTimestampInHeader() {
        controller.refresh();
        String output = outContent.toString();
        assertTrue(
            Pattern.compile("\\d{2}:\\d{2}:\\d{2}").matcher(output).find(),
            "출력에 HH:mm:ss 형식 타임스탬프 포함 여부"
        );
    }
}
