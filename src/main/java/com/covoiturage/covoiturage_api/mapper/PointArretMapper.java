package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.response.PointArretResponse;
import com.covoiturage.covoiturage_api.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    @Mapping(source = "heurePassage", target = "dateHeure")
    @Mapping(source = "prixDepuisDepart", target = "prix")
    PointArretResponse toResponse(PointArret pointArret);
}
