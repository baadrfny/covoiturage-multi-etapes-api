package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.PointArret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointArretRepository extends JpaRepository<PointArret, Long> {
}
