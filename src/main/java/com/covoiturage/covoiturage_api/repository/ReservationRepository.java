package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Reservation;
import com.covoiturage.covoiturage_api.entity.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTrajetIdAndStatut(Long trajetId, StatutReservation statut);

    @Query("""
            select coalesce(sum(r.nombrePlaces), 0)
            from Reservation r
            where r.trajet.id = :trajetId
              and r.statut = :statut
            """)
    long sumNombrePlacesByTrajetIdAndStatut(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut);
}
