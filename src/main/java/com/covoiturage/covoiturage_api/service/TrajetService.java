package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.TrajetRechercheResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TrajetService {

    TrajetResponse createTrajet(TrajetRequest request);

    TrajetResponse getTrajetById(Long id);

    List<TrajetResponse> getTrajetsByConducteur(Long conducteurId);

    Page<TrajetRechercheResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate dateDepart,
            BigDecimal prixMax,
            Integer placesMin,
            Pageable pageable
    );

    TrajetResponse changeStatut(Long id, StatutTrajet statut);
}
