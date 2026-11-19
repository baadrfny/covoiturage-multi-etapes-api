
package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.VehiculeResponse;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse createVehicule(VehiculeRequest request);

    VehiculeResponse getVehiculeById(Long id);

    List<VehiculeResponse> getVehiculesByConducteur(Long conducteurId);

    VehiculeResponse updateVehicule(Long id, VehiculeRequest request);

    void deleteVehicule(Long id);
}
