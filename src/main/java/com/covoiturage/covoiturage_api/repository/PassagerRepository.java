package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassagerRepository extends JpaRepository<Passager, Long> {

}
