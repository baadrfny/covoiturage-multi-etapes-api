
package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import com.covoiturage.covoiturage_api.mapper.PassagerMapper;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.service.PassagerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log =
            LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository repository;
    private final PassagerMapper mapper;

    public PassagerServiceImpl(PassagerRepository repository,
                               PassagerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PassagerResponse createPassager(PassagerRequest request) {
        if (repository.existsByEmail(request.email())) {
            log.warn("Passager creation rejected: email already exists");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        Passager passager = mapper.toEntity(request);
        Passager saved = repository.save(passager);
        log.info("Passager created successfully with ID: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public List<PassagerResponse> getAllPassagers() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public PassagerResponse getPassagerById(Long id) {
        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passager not found"));

        return mapper.toResponse(passager);
    }

    @Override
    public PassagerResponse updatePassager(Long id, PassagerRequest request) {
        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passager not found"));

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            log.warn("Passager update rejected: email already exists for ID: {}", id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        passager.setNom(request.nom());
        passager.setPrenom(request.prenom());
        passager.setEmail(request.email());
        passager.setTelephone(request.telephone());

        Passager updated = repository.save(passager);
        log.info("Passager updated successfully with ID: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    public PassagerResponse getPassagerByEmail(String email) {
        Passager passager = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passager not found"));

        return mapper.toResponse(passager);
    }
}
