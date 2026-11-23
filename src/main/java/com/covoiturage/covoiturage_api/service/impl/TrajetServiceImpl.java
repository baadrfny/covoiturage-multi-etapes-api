package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.PointArretResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetRechercheResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.PointArret;
import com.covoiturage.covoiturage_api.entity.Reservation;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.entity.enums.StatutReservation;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.mapper.TrajetMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import com.covoiturage.covoiturage_api.service.TrajetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrajetServiceImpl implements TrajetService {

    private static final Logger log = LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository trajetRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;

    public TrajetServiceImpl(TrajetRepository trajetRepository,
                             VehiculeRepository vehiculeRepository,
                             ConducteurRepository conducteurRepository,
                             ReservationRepository reservationRepository,
                             TrajetMapper trajetMapper) {
        this.trajetRepository = trajetRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.conducteurRepository = conducteurRepository;
        this.reservationRepository = reservationRepository;
        this.trajetMapper = trajetMapper;
    }

    @Override
    @Transactional
    public TrajetResponse createTrajet(TrajetRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

        Conducteur conducteur = vehicule.getConducteur();
        if (request.conducteurId() != null) {
            Conducteur demande = conducteurRepository.findById(request.conducteurId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conducteur introuvable"));
            if (!demande.getId().equals(conducteur.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le véhicule n'appartient pas au conducteur");
            }
            conducteur = demande;
        }

        if (request.placesProposees() == null
                || request.placesProposees() < 1
                || request.placesProposees() > vehicule.getCapacite()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de places invalide");
        }

        validatePointsArret(request.pointsArret());
        List<PointArretRequest> points = request.pointsArret();
        PointArretRequest depart = points.get(0);
        PointArretRequest arrivee = points.get(points.size() - 1);

        Trajet trajet = new Trajet();
        trajet.setVehicule(vehicule);
        trajet.setConducteur(conducteur);
        trajet.setVilleDepart(depart.ville());
        trajet.setVilleArrivee(arrivee.ville());
        trajet.setDateDepart(depart.dateHeure());
        trajet.setPlacesProposees(request.placesProposees());
        trajet.setStatut(StatutTrajet.PUBLIE);

        List<PointArret> pointsArret = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            PointArretRequest p = points.get(i);
            PointArret point = new PointArret();
            point.setTrajet(trajet);
            point.setVille(p.ville().trim());
            point.setOrdre(i);
            point.setHeurePassage(p.dateHeure());
            point.setPrixDepuisDepart(p.prix());
            pointsArret.add(point);
        }
        trajet.setPointsArret(pointsArret);

        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet created with ID: {}", saved.getId());
        return trajetMapper.toResponse(saved, request.placesProposees());
    }

    @Override
    @Transactional
    public TrajetResponse getTrajetById(Long id) {
        return buildResponse(findTrajet(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrajetResponse> getTrajetsByConducteur(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conducteur introuvable");
        }

        List<Trajet> trajets = trajetRepository.findByConducteurId(conducteurId);
        if (trajets.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> reservees = mapPlacesReservees(trajets.stream().map(Trajet::getId).toList());
        return trajets.stream()
                .map(t -> trajetMapper.toResponse(t, placesRestantes(t, reservees.getOrDefault(t.getId(), 0L))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrajetRechercheResponse> rechercherTrajets(
            String villeDepart, String villeArrivee, LocalDate dateDepart,
            BigDecimal prixMax, Integer placesMin, Pageable pageable) {

        if (villeDepart == null || villeDepart.isBlank()
                || villeArrivee == null || villeArrivee.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Les villes de départ et d'arrivée sont obligatoires");
        }

        String dep = villeDepart.trim();
        String arr = villeArrivee.trim();
        LocalDateTime dateDebut = dateDepart != null ? dateDepart.atStartOfDay() : LocalDateTime.now();
        LocalDateTime dateFin = dateDepart != null ? dateDepart.plusDays(1).atStartOfDay() : null;
        Integer minPlaces = placesMin != null ? placesMin : 1;

        Page<Trajet> page = trajetRepository.rechercherTrajets(
                dep, arr, StatutTrajet.PUBLIE, dateDebut, dateFin, prixMax, minPlaces, pageable);

        if (page.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }

        List<Long> ids = page.getContent().stream().map(Trajet::getId).toList();
        Map<Long, Trajet> details = trajetRepository.findWithDetailsByIds(ids).stream()
                .collect(Collectors.toMap(Trajet::getId, t -> t));

        Map<Long, Long> reservees = mapPlacesReservees(ids);

        List<TrajetRechercheResponse> content = page.getContent().stream()
                .map(t -> details.getOrDefault(t.getId(), t))
                .map(t -> buildRechercheResponse(t, dep, arr, reservees.getOrDefault(t.getId(), 0L)))
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public TrajetResponse changeStatut(Long id, StatutTrajet statut) {
        if (statut == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut obligatoire");
        }

        Trajet trajet = findTrajet(id);
        StatutTrajet actuel = trajet.getStatut();
        if (actuel == statut) {
            return buildResponse(trajet);
        }
        if (actuel == StatutTrajet.TERMINE || actuel == StatutTrajet.ANNULE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un trajet terminé ou annulé ne peut plus changer de statut");
        }

        boolean complet = isTrajetComplet(trajet);
        switch (statut) {
            case TERMINE -> {
                if (!LocalDateTime.now().isAfter(trajet.getDateDepart())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Le trajet ne peut être terminé qu'après le départ");
                }
            }
            case ANNULE -> annulerReservations(trajet.getId());
            case COMPLET -> {
                if (!complet) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Toutes les places ne sont pas encore réservées");
                }
            }
            case PUBLIE -> {
                if (complet) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Aucune place disponible pour republier le trajet");
                }
            }
        }

        trajet.setStatut(statut);
        log.info("Trajet {} status changed from {} to {}", id, actuel, statut);
        return buildResponse(trajet);
    }

    private TrajetResponse buildResponse(Trajet trajet) {
        int restantes = calculerPlacesRestantes(trajet);
        synchroniserStatut(trajet, isTrajetComplet(trajet));
        return trajetMapper.toResponse(trajet, restantes);
    }

    private TrajetRechercheResponse buildRechercheResponse(
            Trajet trajet, String villeDepart, String villeArrivee, long placesReservees) {

        int restantes = (int) Math.max((long) trajet.getPlacesProposees() - placesReservees, 0);
        TrajetResponse base = trajetMapper.toResponse(trajet, restantes);
        PointArretResponse m = trouverPointResponse(base.pointsArret(), villeDepart);
        PointArretResponse d = trouverPointResponse(base.pointsArret(), villeArrivee);
        BigDecimal prixSegment = d.prix().subtract(m.prix());

        return new TrajetRechercheResponse(
                base.id(), base.conducteur(), base.vehicule(), base.villeDepart(), base.villeArrivee(),
                base.dateDepart(), base.placesProposees(), base.placesRestantes(), base.statut(),
                base.pointsArret(), m, d, prixSegment);
    }

    private PointArretResponse trouverPointResponse(List<PointArretResponse> points, String ville) {
        return points.stream()
                .filter(p -> p.ville().equalsIgnoreCase(ville))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Point d'arrêt introuvable pour la ville " + ville));
    }

    private Trajet findTrajet(Long id) {
        return trajetRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable"));
    }

    private boolean isTrajetComplet(Trajet trajet) {
        long reservees = reservationRepository.sumNombrePlacesByTrajetIdAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);
        return reservees >= trajet.getPlacesProposees();
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        long reservees = reservationRepository.sumNombrePlacesByTrajetIdAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);
        return (int) Math.max((long) trajet.getPlacesProposees() - reservees, 0);
    }

    private Map<Long, Long> mapPlacesReservees(List<Long> trajetIds) {
        Map<Long, Long> map = new HashMap<>();
        reservationRepository.sumPlacesByTrajetIds(trajetIds, StatutReservation.CONFIRMEE)
                .forEach(row -> map.put((Long) row[0], ((Number) row[1]).longValue()));
        return map;
    }

    private int placesRestantes(Trajet trajet, long reservees) {
        return (int) Math.max((long) trajet.getPlacesProposees() - reservees, 0);
    }

    private void synchroniserStatut(Trajet trajet, boolean complet) {
        if (trajet.getStatut() == StatutTrajet.PUBLIE && complet) {
            trajet.setStatut(StatutTrajet.COMPLET);
        } else if (trajet.getStatut() == StatutTrajet.COMPLET && !complet) {
            trajet.setStatut(StatutTrajet.PUBLIE);
        }
    }

    private void annulerReservations(Long trajetId) {
        List<Reservation> reservations = reservationRepository
                .findByTrajetIdAndStatut(trajetId, StatutReservation.CONFIRMEE);
        reservations.forEach(r -> r.setStatut(StatutReservation.ANNULEE));
        reservationRepository.saveAll(reservations);
    }

    private void validatePointsArret(List<PointArretRequest> points) {
        if (points == null || points.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un trajet doit contenir au moins 2 points d'arrêt");
        }
        for (PointArretRequest point : points) {
            if (point == null || point.ville() == null || point.ville().isBlank()
                    || point.dateHeure() == null || point.prix() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les informations des points d'arrêt sont obligatoires");
            }
        }
        if (!points.get(0).dateHeure().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de départ doit être dans le futur");
        }
        if (points.get(0).prix().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le prix du premier point doit être 0");
        }

        Set<String> villes = new HashSet<>();
        for (int i = 0; i < points.size(); i++) {
            PointArretRequest point = points.get(i);
            String ville = point.ville().trim().toLowerCase(Locale.ROOT);
            if (!villes.add(ville)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ville ne peut pas être répétée");
            }
            if (i > 0) {
                PointArretRequest precedent = points.get(i - 1);
                if (!point.dateHeure().isAfter(precedent.dateHeure())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Les heures doivent être strictement croissantes");
                }
                if (point.prix().compareTo(precedent.prix()) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Les prix doivent être strictement croissants");
                }
            }
        }
    }
}
