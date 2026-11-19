package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {PointArretMapper.class, ConducteurMapper.class, VehiculeMapper.class}
)
public interface TrajetMapper {

    @Mapping(source = "trajet.conducteur", target = "conducteur")
    @Mapping(source = "trajet.vehicule", target = "vehicule")
    @Mapping(source = "placesRestantes", target = "placesRestantes")
    TrajetResponse toResponse(Trajet trajet, Integer placesRestantes);
}
