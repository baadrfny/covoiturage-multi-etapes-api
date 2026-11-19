package com.covoiturage.covoiturage_api.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_arret")
@Getter
@Setter
@NoArgsConstructor
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @JoinColumn(name = "trajet_id" , nullable = false)
    @ManyToOne
    private Trajet trajet;

    @NotBlank
    @Column(nullable = false)
    private String ville;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    private Integer ordre;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime heurePassage;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    private BigDecimal prixDepuisDepart;


}
