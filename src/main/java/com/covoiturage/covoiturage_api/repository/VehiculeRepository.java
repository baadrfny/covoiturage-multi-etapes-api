package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule , Long> {
}
