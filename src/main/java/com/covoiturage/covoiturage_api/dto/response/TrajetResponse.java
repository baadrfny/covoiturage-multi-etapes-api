package com.covoiturage.covoiturage_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        ConducteurResponse conducteur,
        VehiculeResponse vehicule,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        Integer placesProposees,
        Integer placesRestantes,
        String statut,
        List<PointArretResponse> pointsArret
) {
}
