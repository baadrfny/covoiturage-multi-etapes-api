
package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.service.PassagerService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
public class PassagerController {

    private final PassagerService passagerService;

    public PassagerController(PassagerService passagerService) {
        this.passagerService = passagerService;
    }

    @PostMapping
    public PassagerResponse createPassager(
            @Valid @RequestBody PassagerRequest request) {
        return passagerService.createPassager(request);
    }

    @GetMapping
    public List<PassagerResponse> getAllPassagers() {
        return passagerService.getAllPassagers();
    }

    @GetMapping("/search")
    public PassagerResponse searchByEmail(@RequestParam String email) {
        return passagerService.getPassagerByEmail(email);
    }

    @GetMapping("/{id}")
    public PassagerResponse getPassagerById(@PathVariable Long id) {
        return passagerService.getPassagerById(id);
    }

    @PutMapping("/{id}")
    public PassagerResponse updatePassager(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequest request) {
        return passagerService.updatePassager(id, request);
    }
}
