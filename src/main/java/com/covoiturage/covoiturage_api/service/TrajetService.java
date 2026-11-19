package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;

import java.util.List;

public interface TrajetService {

    TrajetResponse createTrajet(TrajetRequest request);

    TrajetResponse getTrajetById(Long id);

    List<TrajetResponse> getTrajetsByConducteur(Long conducteurId);

    TrajetResponse changeStatut(Long id, StatutTrajet statut);
}
