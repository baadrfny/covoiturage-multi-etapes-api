package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.mapper.ConducteurMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.service.ConducteurService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository repository;
    private final ConducteurMapper mapper;

    public ConducteurServiceImpl(ConducteurRepository repository, ConducteurMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ConducteurResponse createConducteur(ConducteurRequest request) {
        Conducteur saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> getAllConducteurs() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponse getConducteurById(Long id) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conducteur introuvable"));
        return mapper.toResponse(conducteur);
    }

    @Override
    @Transactional
    public ConducteurResponse updateConducteur(Long id, ConducteurRequest request) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conducteur introuvable"));
        conducteur.setNom(request.nom());
        conducteur.setPrenom(request.prenom());
        conducteur.setEmail(request.email());
        conducteur.setTelephone(request.telephone());
        conducteur.setNumeroPermis(request.numeroPermis());
        return mapper.toResponse(repository.save(conducteur));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> searchConducteurs(String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase();
        return repository.findAll().stream()
                .filter(c -> c.getNom().toLowerCase().contains(k) || c.getEmail().toLowerCase().contains(k))
                .map(mapper::toResponse)
                .toList();
    }
}
