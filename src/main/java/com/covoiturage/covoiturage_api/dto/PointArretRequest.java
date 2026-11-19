
package com.covoiturage.covoiturage_api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretRequest(
        @NotBlank String ville,
        @NotNull LocalDateTime dateHeure,
        @NotNull @DecimalMin("0.0") BigDecimal prix
) {}
