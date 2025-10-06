package com.bludots.services;

import com.bludots.entities.TomcatInstanceEntity;
import com.bludots.repositories.TomcatInstanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TomcatInstanceService {

    private final TomcatInstanceRepository repository;

    public TomcatInstanceService(TomcatInstanceRepository repository) {
        this.repository = repository;
    }

    public List<TomcatInstanceEntity> getAll() {
        return repository.findAll();
    }

    public TomcatInstanceEntity save(TomcatInstanceEntity instance) {
        return repository.save(instance);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
