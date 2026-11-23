package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.mapper.TrajetMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock private TrajetRepository trajetRepository;
    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private ConducteurRepository conducteurRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private TrajetMapper trajetMapper;
    @InjectMocks private TrajetServiceImpl trajetService;

    @Test
    void shouldRejectTerminerBeforeDepart() {
        Trajet trajet = new Trajet();
        trajet.setId(1L);
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(LocalDateTime.now().plusHours(2));
        trajet.setPlacesProposees(3);

        when(trajetRepository.findDetailById(1L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.sumNombrePlacesByTrajetIdAndStatut(1L,
                com.covoiturage.covoiturage_api.entity.enums.StatutReservation.CONFIRMEE)).thenReturn(0L);

        assertThatThrownBy(() -> trajetService.changeStatut(1L, StatutTrajet.TERMINE))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
