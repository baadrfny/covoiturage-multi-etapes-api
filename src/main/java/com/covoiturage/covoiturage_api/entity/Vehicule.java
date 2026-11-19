package com.covoiturage.covoiturage_api.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicule")
@Getter
@Setter
@NoArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false , unique = true)
    private String immatriculation;

    @Column(nullable = false)
    private String couleur;

    @NotNull
    @Min(1)
    @Max(8)
    @Column(nullable = false)
    private Integer capacite;

    @ManyToOne
    @JoinColumn(name = "conducteur_id" , nullable = false)
    private Conducteur conducteur;
}
