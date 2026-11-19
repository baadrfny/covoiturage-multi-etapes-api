package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.mapper.ConducteurMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.service.ConducteurService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository repository;
    private final ConducteurMapper mapper;

    public ConducteurServiceImpl(ConducteurRepository repository,
                                 ConducteurMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ConducteurResponse createConducteur(ConducteurRequest request) {
        Conducteur conducteur = mapper.toEntity(request);
        Conducteur saved = repository.save(conducteur);
        return mapper.toResponse(saved);
    }

    @Override
    public List<ConducteurResponse> getAllConducteurs() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ConducteurResponse getConducteurById(Long id) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conducteur introuvable"));

        return mapper.toResponse(conducteur);
    }

    @Override
    public ConducteurResponse updateConducteur(Long id, ConducteurRequest request) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conducteur introuvable"));

        conducteur.setNom(request.nom());
        conducteur.setPrenom(request.prenom());
        conducteur.setEmail(request.email());
        conducteur.setTelephone(request.telephone());
        conducteur.setNumeroPermis(request.numeroPermis());

        Conducteur updated = repository.save(conducteur);
        return mapper.toResponse(updated);
    }

    @Override
    public List<ConducteurResponse> searchConducteurs(String keyword) {
        return repository.findAll()
                .stream()
                .filter(c -> c.getNom().toLowerCase().contains(keyword.toLowerCase())
                        || c.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .map(mapper::toResponse)
                .toList();
    }
}