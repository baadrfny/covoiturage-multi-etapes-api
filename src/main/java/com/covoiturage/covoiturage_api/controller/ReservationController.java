package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @Valid @RequestBody ReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/passager/{passagerId}")
    public List<ReservationResponse> getByPassager(
            @PathVariable Long passagerId) {
        return reservationService.getReservationsByPassager(passagerId);
    }

    @GetMapping("/trajet/{trajetId}")
    public List<ReservationResponse> getByTrajet(
            @PathVariable Long trajetId) {
        return reservationService.getReservationsByTrajet(trajetId);
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<ReservationResponse> getByConducteur(
            @PathVariable Long conducteurId) {
        return reservationService.getReservationsByConducteur(conducteurId);
    }

    @PutMapping("/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annulerReservation(@PathVariable Long id) {
        reservationService.annulerReservation(id);
    }
}