
package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.mapper.VehiculeMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import com.covoiturage.covoiturage_api.service.VehiculeService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository repository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper mapper;

    public VehiculeServiceImpl(
            VehiculeRepository repository,
            ConducteurRepository conducteurRepository,
            TrajetRepository trajetRepository,
            VehiculeMapper mapper) {

        this.repository = repository;
        this.conducteurRepository = conducteurRepository;
        this.trajetRepository = trajetRepository;
        this.mapper = mapper;
    }

    @Override
    public VehiculeResponse createVehicule(VehiculeRequest request) {

        if (repository.existsByImmatriculation(request.immatriculation())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Immatriculation already exists");
        }

        Conducteur conducteur = conducteurRepository
                .findById(request.conducteurId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conducteur not found"));

        Vehicule vehicule = mapper.toEntity(request);
        vehicule.setConducteur(conducteur);

        return mapper.toResponse(repository.save(vehicule));
    }

    @Override
    public VehiculeResponse getVehiculeById(Long id) {
        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicule not found"));

        return mapper.toResponse(vehicule);
    }


    @Override
    public List<VehiculeResponse> getVehiculesByConducteur(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Conducteur not found");
        }

        return repository.findByConducteurId(conducteurId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    @Override
    public VehiculeResponse updateVehicule(Long id, VehiculeRequest request) {

        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicule not found"));

        if (repository.existsByImmatriculationAndIdNot(
                request.immatriculation(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Immatriculation already exists");
        }

        Conducteur conducteur = conducteurRepository
                .findById(request.conducteurId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conducteur not found"));

        vehicule.setMarque(request.marque());
        vehicule.setModele(request.modele());
        vehicule.setImmatriculation(request.immatriculation());
        vehicule.setCouleur(request.couleur());
        vehicule.setCapacite(request.capacite());
        vehicule.setConducteur(conducteur);

        return mapper.toResponse(repository.save(vehicule));
    }

    @Override
    public void deleteVehicule(Long id) {

        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicule not found"));

        if (trajetRepository.existsByVehiculeId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicule associated with a trajet");
        }

        repository.delete(vehicule);
    }
}
