package org.example.model.repository;

import org.example.model.entity.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySampleRepositoryTest {

    SampleRepository repo = new InMemorySampleRepository();

    @Test
    void findAllReturnsThreeDummySamples() {
        assertEquals(3, repo.findAll().size());
    }

    @Test
    void findAllIsUnmodifiable() {
        List<Sample> list = repo.findAll();
        assertThrows(UnsupportedOperationException.class,
            () -> list.add(new Sample(99L, "TestChip", 10, 0.5, 5)));
    }

    @Test
    void findByIdExistingReturnsCorrectSample() {
        Optional<Sample> result = repo.findById(2L);
        assertTrue(result.isPresent());
        Sample sample = result.get();
        assertEquals(2L, sample.getSampleId());
        assertEquals("BetaWafer", sample.getName());
        assertEquals(45, sample.getAvgProductionTime());
        assertEquals(0.75, sample.getYield());
        assertEquals(0, sample.getStock());
    }

    @Test
    void findByIdNonExistingReturnsEmpty() {
        assertTrue(repo.findById(99L).isEmpty());
    }
}
