package org.example.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SampleStatusTest {

    @Test
    void createRecordAllComponentsStored() {
        SampleStatus status = new SampleStatus(1L, "AlphaChip", 120, "여유");
        assertEquals(1L, status.sampleId());
        assertEquals("AlphaChip", status.name());
        assertEquals(120, status.stock());
        assertEquals("여유", status.stockLevel());
    }
}
