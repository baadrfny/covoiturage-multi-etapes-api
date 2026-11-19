
package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.VehiculeResponse;
import com.covoiturage.covoiturage_api.service.VehiculeService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    @PostMapping
    public VehiculeResponse createVehicule(
            @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.createVehicule(request);
    }

    @GetMapping("/{id}")
    public VehiculeResponse getVehiculeById(@PathVariable Long id) {
        return vehiculeService.getVehiculeById(id);
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<VehiculeResponse> getVehiculesByConducteur(
            @PathVariable Long conducteurId) {
        return vehiculeService.getVehiculesByConducteur(conducteurId);
    }

    @PutMapping("/{id}")
    public VehiculeResponse updateVehicule(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.updateVehicule(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }
}
