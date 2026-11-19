package com.covoiturage.covoiturage_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrajetRequest(
        Long conducteurId,

        @NotNull
        Long vehiculeId,

        @NotNull
        @Min(1)
        Integer placesProposees,

        @NotNull
        @Size(min = 2)
        List<@Valid PointArretRequest> pointsArret
) {
}
