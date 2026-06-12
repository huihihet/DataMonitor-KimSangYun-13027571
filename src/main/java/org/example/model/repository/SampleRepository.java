package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    List<Sample> findAll();
    Optional<Sample> findById(Long id);
}
