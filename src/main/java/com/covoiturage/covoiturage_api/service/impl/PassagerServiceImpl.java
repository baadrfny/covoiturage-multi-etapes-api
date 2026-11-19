
package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import com.covoiturage.covoiturage_api.mapper.PassagerMapper;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.service.PassagerService;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class PassagerServiceImpl implements PassagerService {

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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        Passager passager = mapper.toEntity(request);
        return mapper.toResponse(repository.save(passager));
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        passager.setNom(request.nom());
        passager.setPrenom(request.prenom());
        passager.setEmail(request.email());
        passager.setTelephone(request.telephone());

        return mapper.toResponse(repository.save(passager));
    }

    @Override
    public PassagerResponse getPassagerByEmail(String email) {
        Passager passager = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passager not found"));

        return mapper.toResponse(passager);
    }
}
