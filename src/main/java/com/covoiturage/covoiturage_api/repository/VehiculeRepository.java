
package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculation(String immatriculation);

    boolean existsByImmatriculationAndIdNot(String immatriculation, Long id);

    List<Vehicule> findByConducteurId(Long conducteurId);
}
