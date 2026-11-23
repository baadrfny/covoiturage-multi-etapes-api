package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN FETCH t.conducteur
    JOIN FETCH t.vehicule
    LEFT JOIN FETCH t.pointsArret
    WHERE t.conducteur.id = :conducteurId
    """)
    List<Trajet> findByConducteurId(@Param("conducteurId") Long conducteurId);

    List<Trajet> findByVehiculeConducteurId(Long conducteurId);

    @Query("""
    SELECT DISTINCT t FROM Trajet t
    JOIN FETCH t.conducteur
    JOIN FETCH t.vehicule
    LEFT JOIN FETCH t.pointsArret
    WHERE t.id = :id
    """)
    Optional<Trajet> findDetailById(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN FETCH t.conducteur
    JOIN FETCH t.vehicule
    LEFT JOIN FETCH t.pointsArret
    WHERE t.id IN :ids
    """)
    List<Trajet> findWithDetailsByIds(@Param("ids") List<Long> ids);

    @Query(
            value = """
                SELECT DISTINCT t
                FROM Trajet t
                JOIN t.pointsArret depart
                JOIN t.pointsArret arrivee
                WHERE LOWER(depart.ville) = LOWER(:villeDepart)
                  AND LOWER(arrivee.ville) = LOWER(:villeArrivee)
                  AND depart.ordre < arrivee.ordre
                  AND t.statut = :statut
                  AND (:dateDepart IS NULL OR depart.heurePassage >= :dateDepart)
                  AND (:dateFin IS NULL OR depart.heurePassage < :dateFin)
                  AND (:prixMax IS NULL OR (arrivee.prixDepuisDepart - depart.prixDepuisDepart) <= :prixMax)
                  AND (
                      :placesMin IS NULL
                      OR (t.placesProposees - (
                          SELECT COALESCE(SUM(r.nombrePlaces), 0)
                          FROM Reservation r
                          WHERE r.trajet = t
                            AND r.statut = com.covoiturage.covoiturage_api.entity.enums.StatutReservation.CONFIRMEE
                      )) >= :placesMin
                  )
                """,
            countQuery = """
                SELECT COUNT(DISTINCT t)
                FROM Trajet t
                JOIN t.pointsArret depart
                JOIN t.pointsArret arrivee
                WHERE LOWER(depart.ville) = LOWER(:villeDepart)
                  AND LOWER(arrivee.ville) = LOWER(:villeArrivee)
                  AND depart.ordre < arrivee.ordre
                  AND t.statut = :statut
                  AND (:dateDepart IS NULL OR depart.heurePassage >= :dateDepart)
                  AND (:dateFin IS NULL OR depart.heurePassage < :dateFin)
                  AND (:prixMax IS NULL OR (arrivee.prixDepuisDepart - depart.prixDepuisDepart) <= :prixMax)
                  AND (
                      :placesMin IS NULL
                      OR (t.placesProposees - (
                          SELECT COALESCE(SUM(r.nombrePlaces), 0)
                          FROM Reservation r
                          WHERE r.trajet = t
                            AND r.statut = com.covoiturage.covoiturage_api.entity.enums.StatutReservation.CONFIRMEE
                      )) >= :placesMin
                  )
                """
    )
    Page<Trajet> rechercherTrajets(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("statut") StatutTrajet statut,
            @Param("dateDepart") LocalDateTime dateDepart,
            @Param("dateFin") LocalDateTime dateFin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("placesMin") Integer placesMin,
            Pageable pageable
    );
}
