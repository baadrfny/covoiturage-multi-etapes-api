package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    PassagerResponse toResponse(Passager passager);
    Passager toEntity(PassagerRequest request);

}
