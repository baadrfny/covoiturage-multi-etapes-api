package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.ChangerStatutRequest;
import com.covoiturage.covoiturage_api.dto.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.service.TrajetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    @PostMapping
    public TrajetResponse createTrajet(
            @Valid @RequestBody TrajetRequest request) {
        return trajetService.createTrajet(request);
    }

    @GetMapping("/{id}")
    public TrajetResponse getTrajetById(@PathVariable Long id) {
        return trajetService.getTrajetById(id);
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<TrajetResponse> getTrajetsByConducteur(
            @PathVariable Long conducteurId) {
        return trajetService.getTrajetsByConducteur(conducteurId);
    }

    @PutMapping("/{id}/statut")
    public TrajetResponse changeStatut(
            @PathVariable Long id,
            @Valid @RequestBody ChangerStatutRequest request) {
        return trajetService.changeStatut(id, request.statut());
    }
}
