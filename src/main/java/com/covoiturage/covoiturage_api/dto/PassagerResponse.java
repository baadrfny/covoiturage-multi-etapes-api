package com.covoiturage.covoiturage_api.dto;

public record PassagerResponse(
        Long id ,
        String nom,
        String prenom,
        String email,
        String telephone
) {
}
