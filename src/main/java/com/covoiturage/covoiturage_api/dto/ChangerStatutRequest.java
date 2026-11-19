package com.covoiturage.covoiturage_api.dto;

import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import jakarta.validation.constraints.NotNull;

public record ChangerStatutRequest(
        @NotNull StatutTrajet statut
) {
}
