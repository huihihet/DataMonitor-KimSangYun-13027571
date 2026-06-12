package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InMemorySampleRepository implements SampleRepository {
    private final List<Sample> store;

    public InMemorySampleRepository() {
        this.store = new ArrayList<>(List.of(
            new Sample(1L, "AlphaChip", 30, 0.90, 120),
            new Sample(2L, "BetaWafer", 45, 0.75,   0),
            new Sample(3L, "GammaCore", 60, 0.85,  15)
        ));
    }

    @Override
    public List<Sample> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public Optional<Sample> findById(Long id) {
        return store.stream()
                    .filter(s -> s.getSampleId().equals(id))
                    .findFirst();
    }
}
