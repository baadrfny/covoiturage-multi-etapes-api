package com.covoiturage.covoiturage_api.dto.response;

import com.covoiturage.covoiturage_api.entity.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long passagerId,
        Long trajetId,
        Long pointMonteeId,
        Long pointDescenteId,
        Integer nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut
) {}