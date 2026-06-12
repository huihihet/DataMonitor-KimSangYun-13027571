package org.example.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SampleTest {

    @Test
    void createSampleAllFieldsStored() {
        Sample sample = new Sample(1L, "AlphaChip", 30, 0.90, 120);
        assertEquals(1L, sample.getSampleId());
        assertEquals("AlphaChip", sample.getName());
        assertEquals(30, sample.getAvgProductionTime());
        assertEquals(0.90, sample.getYield());
        assertEquals(120, sample.getStock());
    }

    @Test
    void createSampleNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(null, "AlphaChip", 30, 0.90, 120));
    }

    @Test
    void createSampleYieldAtMax() {
        assertDoesNotThrow(() -> new Sample(1L, "AlphaChip", 30, 1.0, 120));
    }

    @Test
    void createSampleYieldAtMinBoundary() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, "AlphaChip", 30, 0.0, 120));
    }

    @Test
    void createSampleYieldOverMax() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, "AlphaChip", 30, 1.001, 120));
    }

    @Test
    void createSampleAvgProductionTimeAtMin() {
        assertDoesNotThrow(() -> new Sample(1L, "AlphaChip", 1, 0.90, 120));
    }

    @Test
    void createSampleAvgProductionTimeZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, "AlphaChip", 0, 0.90, 120));
    }

    @Test
    void createSampleStockAtMin() {
        assertDoesNotThrow(() -> new Sample(1L, "AlphaChip", 30, 0.90, 0));
    }

    @Test
    void createSampleNegativeStock() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, "AlphaChip", 30, 0.90, -1));
    }

    @Test
    void createSampleNullName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, null, 30, 0.90, 120));
    }

    @Test
    void createSampleBlankName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample(1L, "  ", 30, 0.90, 120));
    }
}
