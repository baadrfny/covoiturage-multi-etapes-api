package com.covoiturage.covoiturage_api.service.impl;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import com.covoiturage.covoiturage_api.service.impl.VehiculeServiceImpl;

import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;
    @InjectMocks
    private VehiculeServiceImpl vehiculeService;


    @Test
    void shouldThrowExceptionWhenVehiculeNotFound(){
        //Given
        Long vehiculeId = 999L;
        when(vehiculeRepository.findById(vehiculeId))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> vehiculeService.getVehiculeById(vehiculeId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Vehicule not found")
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

}

