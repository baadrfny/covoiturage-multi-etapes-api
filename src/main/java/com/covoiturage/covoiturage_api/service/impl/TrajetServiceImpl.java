package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.TrajetRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;

    public TrajetServiceImpl(
            TrajetRepository trajetRepository,
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

        Vehicule vehicule = vehiculeRepository
                .findById(request.vehiculeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Véhicule introuvable"));

        Conducteur conducteur = vehicule.getConducteur();

        if (request.conducteurId() != null) {

            Conducteur conducteurDemande = conducteurRepository
                    .findById(request.conducteurId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Conducteur introuvable"));

            if (!conducteurDemande.getId().equals(conducteur.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Le véhicule n'appartient pas au conducteur");
            }

            conducteur = conducteurDemande;
        }

        if (request.placesProposees() == null
                || request.placesProposees() < 1
                || request.placesProposees() > vehicule.getCapacite()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de places invalide");
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

            PointArretRequest pointRequest = points.get(i);

            PointArret point = new PointArret();

            point.setTrajet(trajet);
            point.setVille(pointRequest.ville().trim());
            point.setOrdre(i);
            point.setHeurePassage(pointRequest.dateHeure());
            point.setPrixDepuisDepart(pointRequest.prix());

            pointsArret.add(point);
        }

        trajet.setPointsArret(pointsArret);

        Trajet savedTrajet = trajetRepository.save(trajet);

        return trajetMapper.toResponse(savedTrajet, request.placesProposees());
    }

    @Override
    @Transactional
    public TrajetResponse getTrajetById(Long id) {

        Trajet trajet = findTrajet(id);

        return buildResponse(trajet);
    }

    @Override
    @Transactional
    public List<TrajetResponse> getTrajetsByConducteur(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Conducteur introuvable");
        }

        return trajetRepository.findByConducteurId(conducteurId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrajetResponse changeStatut(Long id, StatutTrajet statut) {

        if (statut == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Statut obligatoire");
        }

        Trajet trajet = findTrajet(id);

        StatutTrajet statutActuel = trajet.getStatut();

        if (statutActuel == statut) {
            return buildResponse(trajet);
        }

        if (statutActuel == StatutTrajet.TERMINE
                || statutActuel == StatutTrajet.ANNULE) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un trajet terminé ou annulé ne peut plus changer de statut");
        }

        int placesRestantes = calculerPlacesRestantes(trajet);

        switch (statut) {

            case TERMINE -> {
                if (!LocalDateTime.now().isAfter(trajet.getDateDepart())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Le trajet ne peut être terminé qu'après le départ");
                }
            }

            case ANNULE -> annulerReservations(trajet.getId());

            case COMPLET -> {
                if (placesRestantes > 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Toutes les places ne sont pas encore réservées");
                }
            }

            case PUBLIE -> {
                if (placesRestantes <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Aucune place disponible pour republier le trajet");
                }
            }
        }

        trajet.setStatut(statut);

        return buildResponse(trajet);
    }

    private TrajetResponse buildResponse(Trajet trajet) {

        int placesRestantes = calculerPlacesRestantes(trajet);

        synchroniserStatut(trajet, placesRestantes);

        return trajetMapper.toResponse(trajet, placesRestantes);
    }

    private Trajet findTrajet(Long id) {

        return trajetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trajet introuvable"));
    }

    private int calculerPlacesRestantes(Trajet trajet) {

        long placesReservees = reservationRepository
                .sumNombrePlacesByTrajetIdAndStatut(
                        trajet.getId(), StatutReservation.CONFIRMEE);

        long placesRestantes = (long) trajet.getPlacesProposees() - placesReservees;

        return (int) Math.max(placesRestantes, 0);
    }

    private void synchroniserStatut(Trajet trajet, int placesRestantes) {

        if (trajet.getStatut() == StatutTrajet.PUBLIE && placesRestantes == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
        } else if (trajet.getStatut() == StatutTrajet.COMPLET
                && placesRestantes > 0) {
            trajet.setStatut(StatutTrajet.PUBLIE);
        }
    }

    private void annulerReservations(Long trajetId) {

        List<Reservation> reservations = reservationRepository
                .findByTrajetIdAndStatut(trajetId, StatutReservation.CONFIRMEE);

        reservations.forEach(reservation ->
                reservation.setStatut(StatutReservation.ANNULEE));

        reservationRepository.saveAll(reservations);
    }

    private void validatePointsArret(List<PointArretRequest> points) {

        if (points == null || points.size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Un trajet doit contenir au moins 2 points d'arrêt");
        }

        for (PointArretRequest point : points) {

            if (point == null
                    || point.ville() == null
                    || point.ville().isBlank()
                    || point.dateHeure() == null
                    || point.prix() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Les informations des points d'arrêt sont obligatoires");
            }
        }

        if (!points.get(0).dateHeure().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de départ doit être dans le futur");
        }

        if (points.get(0).prix().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le prix du premier point doit être 0");
        }

        Set<String> villes = new HashSet<>();

        for (int i = 0; i < points.size(); i++) {

            PointArretRequest point = points.get(i);

            String ville = point.ville().trim().toLowerCase(Locale.ROOT);

            if (!villes.add(ville)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Une ville ne peut pas être répétée");
            }

            if (i > 0) {

                PointArretRequest precedent = points.get(i - 1);

                if (!point.dateHeure().isAfter(precedent.dateHeure())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Les heures doivent être strictement croissantes");
                }

                if (point.prix().compareTo(precedent.prix()) <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Les prix doivent être strictement croissants");
                }
            }
        }
    }
}
