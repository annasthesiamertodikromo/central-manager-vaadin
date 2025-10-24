package com.bludots.repositories;

import com.bludots.entities.TomcatInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TomcatInstanceRepository extends JpaRepository<TomcatInstanceEntity, Long> {

    // Find all by status
    List<TomcatInstanceEntity> findByStatusIgnoreCase(String status);

    // Case-insensitive search by name OR status
    List<TomcatInstanceEntity> findByNameContainingIgnoreCaseOrStatusContainingIgnoreCase(String name, String status);

    // For future: Search by client name only
    List<TomcatInstanceEntity> findByNameContainingIgnoreCase(String name);

}
