package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.service.ConducteurService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
public class ConducteurController {

    private final ConducteurService conducteurService;

    public ConducteurController(ConducteurService conducteurService) {
        this.conducteurService = conducteurService;
    }

    @PostMapping
    public ConducteurResponse createConducteur(
            @Valid @RequestBody ConducteurRequest request) {
        return conducteurService.createConducteur(request);
    }

    @GetMapping
    public List<ConducteurResponse> getAllConducteurs() {
        return conducteurService.getAllConducteurs();
    }

    @GetMapping("/{id}")
    public ConducteurResponse getConducteurById(@PathVariable Long id) {
        return conducteurService.getConducteurById(id);
    }

    @PutMapping("/{id}")
    public ConducteurResponse updateConducteur(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequest request) {
        return conducteurService.updateConducteur(id, request);
    }



    @GetMapping("/search")
    public List<ConducteurResponse> searchConducteurs(
            @RequestParam String keyword) {
        return conducteurService.searchConducteurs(keyword);
    }
}