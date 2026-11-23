
package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.entity.*;
import com.covoiturage.covoiturage_api.entity.enums.*;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.repository.*;
import com.covoiturage.covoiturage_api.service.ReservationService;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            PassagerRepository passagerRepository,
            TrajetRepository trajetRepository,
            ConducteurRepository conducteurRepository,
            ReservationMapper reservationMapper) {

        this.reservationRepository = reservationRepository;
        this.passagerRepository = passagerRepository;
        this.trajetRepository = trajetRepository;
        this.conducteurRepository = conducteurRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {

        Passager passager = passagerRepository.findById(request.passagerId())
                .orElseThrow(() -> {
                    log.warn("Reservation rejected: passager {} not found",
                            request.passagerId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Passager introuvable");
                });

        Trajet trajet = trajetRepository.findById(request.trajetId())
                .orElseThrow(() -> {
                    log.warn("Reservation rejected: trajet {} not found",
                            request.trajetId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Trajet introuvable");
                });

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn("Reservation rejected: trajet {} is unavailable with status {}",
                    trajet.getId(), trajet.getStatut());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Trajet indisponible");
        }

        if (!trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            log.warn("Reservation rejected: trajet {} has already started", trajet.getId());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Le trajet a déjà commencé");
        }

        PointArret montee = null;
        PointArret descente = null;

        for (PointArret point : trajet.getPointsArret()) {
            if (point.getId().equals(request.pointMonteeId())) {
                montee = point;
            }
            if (point.getId().equals(request.pointDescenteId())) {
                descente = point;
            }
        }

        if (montee == null) {
            log.warn("Reservation rejected: boarding point {} not found for trajet {}",
                    request.pointMonteeId(), trajet.getId());
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Point de montée introuvable");
        }

        if (descente == null) {
            log.warn("Reservation rejected: destination point {} not found for trajet {}",
                    request.pointDescenteId(), trajet.getId());
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Point de descente introuvable");
        }

        if (montee.getOrdre() >= descente.getOrdre()) {
            log.warn("Reservation rejected: boarding point must precede destination for trajet {}",
                    trajet.getId());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le point de montée doit précéder la descente");
        }

        if (!montee.getHeurePassage().isAfter(LocalDateTime.now())) {
            log.warn("Reservation rejected: boarding point {} has already passed for trajet {}",
                    montee.getId(), trajet.getId());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Le point de montée est déjà passé");
        }

        if (request.nombrePlaces() > trajet.getPlacesProposees()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de places invalide");
        }

        List<Reservation> reservationsConfirmees = reservationRepository.findByTrajetIdAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);

        for (Reservation reservation : reservationsConfirmees) {
            if (reservation.getPassager().getId().equals(passager.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le passager a déjà une réservation confirmée pour ce trajet");
            }
        }

        long placesOccupees = reservationsConfirmees.stream()
                .mapToLong(Reservation::getNombrePlaces)
                .sum();

        long placesDisponibles = trajet.getPlacesProposees() - placesOccupees;
        if (request.nombrePlaces() > placesDisponibles) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de places insuffisant");
        }

        BigDecimal prixUnitaire = descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart());

        BigDecimal montantTotal = prixUnitaire
                .multiply(BigDecimal.valueOf(request.nombrePlaces()))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        Reservation reservation = new Reservation();
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(montee);
        reservation.setPointDescente(descente);
        reservation.setNombrePlaces(request.nombrePlaces());
        reservation.setMontantTotal(montantTotal);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", saved.getId());

        synchroniserStatut(trajet);

        return reservationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReservationResponse getReservationById(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Réservation introuvable"));

        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPassager(Long passagerId) {

        if (!passagerRepository.existsById(passagerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Passager introuvable");
        }

        return reservationRepository.findByPassagerId(passagerId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<ReservationResponse> getReservationsByTrajet(Long trajetId) {

        if (!trajetRepository.existsById(trajetId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Trajet introuvable");
        }

        return reservationRepository.findByTrajetId(trajetId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<ReservationResponse> getReservationsByConducteur(
            Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Conducteur introuvable");
        }

        return reservationRepository.findByTrajetConducteurId(conducteurId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponse annulerReservation(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reservation cancellation rejected: reservation {} not found", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Réservation introuvable");
                });

        Trajet trajet = reservation.getTrajet();

        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            log.warn("Reservation cancellation rejected: reservation {} is already cancelled", id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Réservation déjà annulée");
        }

        if (!trajet.getDateDepart().isAfter(
                LocalDateTime.now().plusHours(24))) {
            log.warn("Reservation cancellation rejected: reservation {} is less than 24 hours before departure",
                    id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Annulation impossible moins de 24 heures avant le départ");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);
        log.info("Reservation cancelled successfully with ID: {}", id);

        synchroniserStatut(trajet);

        return reservationMapper.toResponse(reservation);
    }

    private void synchroniserStatut(Trajet trajet) {
        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {
            return;
        }
        long occupees = reservationRepository.sumNombrePlacesByTrajetIdAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);
        trajet.setStatut(occupees >= trajet.getPlacesProposees() ? StatutTrajet.COMPLET : StatutTrajet.PUBLIE);
        trajetRepository.save(trajet);
    }
}
