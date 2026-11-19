package com.covoiturage.covoiturage_api.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passager")
@Getter
@Setter
@NoArgsConstructor
public class Passager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false , unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;


}
