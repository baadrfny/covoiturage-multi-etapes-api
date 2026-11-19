package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface PassagerRepository extends JpaRepository<Passager, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Passager> findByEmail(String email);

}
