package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.entity.Passager;

import java.util.List;

public interface PassagerService {

    PassagerResponse createPassager(PassagerRequest request);

    List<PassagerResponse> getAllPassagers();

    PassagerResponse getPassagerById(Long id);

    PassagerResponse updatePassager(Long id , PassagerRequest request);

    PassagerResponse getPassagerByEmail(String email);


}
