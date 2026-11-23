package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Reservation;
import com.covoiturage.covoiturage_api.entity.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassagerId(Long passagerId);

    List<Reservation> findByTrajetId(Long trajetId);

    List<Reservation> findByTrajetConducteurId(Long conducteurId);

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.trajet.id = :trajetId
          AND r.statut = :statut
          AND r.pointMontee.ordre < :ordreDescente
          AND r.pointDescente.ordre > :ordreMontee
    """)
    List<Reservation> findReservationsChevauchantes(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut,
            @Param("ordreMontee") Integer ordreMontee,
            @Param("ordreDescente") Integer ordreDescente
    );

    List<Reservation> findByTrajetIdAndStatut(Long trajetId, StatutReservation statut);

    List<Reservation> findByTrajetIdInAndStatut(List<Long> trajetIds, StatutReservation statut);

    @Query("""
        SELECT DISTINCT r FROM Reservation r
        JOIN FETCH r.pointMontee
        JOIN FETCH r.pointDescente
        WHERE r.trajet.id IN :trajetIds AND r.statut = :statut
    """)
    List<Reservation> findByTrajetIdInAndStatutWithPoints(
            @Param("trajetIds") List<Long> trajetIds,
            @Param("statut") StatutReservation statut);

    @Query("""
            select coalesce(sum(r.nombrePlaces), 0)
            from Reservation r
            where r.trajet.id = :trajetId
              and r.statut = :statut
            """)
    long sumNombrePlacesByTrajetIdAndStatut(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut);


    @Query("""
    SELECT r.trajet.id, SUM(r.nombrePlaces)
    FROM Reservation r
    WHERE r.trajet.id IN :trajetIds
      AND r.statut = :statut
    GROUP BY r.trajet.id
    """)
    List<Object[]> sumPlacesByTrajetIds(
            @Param("trajetIds") List<Long> trajetIds,
            @Param("statut") StatutReservation statut
    );
}