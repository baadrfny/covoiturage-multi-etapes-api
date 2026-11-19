package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.ConducteurResponse;
import java.util.List;

public interface ConducteurService {

    ConducteurResponse createConducteur(ConducteurRequest request);

    List<ConducteurResponse> getAllConducteurs();

    ConducteurResponse getConducteurById(Long id);

    ConducteurResponse updateConducteur(Long id, ConducteurRequest request);

    List<ConducteurResponse> searchConducteurs(String keyword);
}