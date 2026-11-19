
package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(source = "conducteur.id", target = "conducteurId")
    VehiculeResponse toResponse(Vehicule vehicule);

    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);
}
