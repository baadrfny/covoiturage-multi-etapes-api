package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface ConducteurMapper {

    ConducteurResponse toResponse(Conducteur conducteur);

    Conducteur toEntity(ConducteurRequest request);
}