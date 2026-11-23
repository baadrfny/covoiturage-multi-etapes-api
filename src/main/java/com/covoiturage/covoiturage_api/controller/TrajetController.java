package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.ChangerStatutRequest;
import com.covoiturage.covoiturage_api.dto.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.TrajetRechercheResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.service.TrajetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    @PostMapping
    public TrajetResponse createTrajet(@Valid @RequestBody TrajetRequest request) {
        return trajetService.createTrajet(request);
    }

    @GetMapping("/recherche")
    public Page<TrajetRechercheResponse> rechercherTrajets(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer placesMin,
            @PageableDefault(size = 10, sort = "dateDepart", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable) {

        return trajetService.rechercherTrajets(
                villeDepart, villeArrivee, dateDepart, prixMax, placesMin, pageable);
    }

    @GetMapping("/{id}")
    public TrajetResponse getTrajetById(@PathVariable Long id) {
        return trajetService.getTrajetById(id);
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<TrajetResponse> getTrajetsByConducteur(@PathVariable Long conducteurId) {
        return trajetService.getTrajetsByConducteur(conducteurId);
    }

    @PutMapping("/{id}/statut")
    public org.springframework.http.ResponseEntity<TrajetResponse> changeStatut(
            @PathVariable Long id,
            @Valid @RequestBody ChangerStatutRequest request) {
        TrajetResponse response = trajetService.changeStatut(id, request.statut());
        if (request.statut() == com.covoiturage.covoiturage_api.entity.enums.StatutTrajet.ANNULE) {
            return org.springframework.http.ResponseEntity.noContent().build();
        }
        return org.springframework.http.ResponseEntity.ok(response);
    }
}
