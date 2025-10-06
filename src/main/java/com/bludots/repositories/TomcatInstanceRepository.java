package com.bludots.repositories;

import com.bludots.entities.TomcatInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TomcatInstanceRepository extends JpaRepository<TomcatInstanceEntity, Long> {

}
