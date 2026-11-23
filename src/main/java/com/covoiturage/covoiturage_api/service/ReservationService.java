package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request);

    ReservationResponse getReservationById(Long id);

    List<ReservationResponse> getReservationsByPassager(Long passagerId);

    List<ReservationResponse> getReservationsByTrajet(Long trajetId);

    List<ReservationResponse> getReservationsByConducteur(Long conducteurId);

    ReservationResponse annulerReservation(Long id);
}