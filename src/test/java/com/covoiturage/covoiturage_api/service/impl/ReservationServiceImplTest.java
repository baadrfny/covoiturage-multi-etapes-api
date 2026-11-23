package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.ReservationRequest;
import com.covoiturage.covoiturage_api.entity.*;
import com.covoiturage.covoiturage_api.entity.enums.StatutReservation;
import com.covoiturage.covoiturage_api.entity.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private PassagerRepository passagerRepository;
    @Mock private TrajetRepository trajetRepository;
    @Mock private ConducteurRepository conducteurRepository;
    @Mock private ReservationMapper reservationMapper;
    @InjectMocks private ReservationServiceImpl reservationService;

    @Test
    void shouldRejectWhenTrajetIsFullGlobally() {
        Passager passager = new Passager();
        passager.setId(1L);

        Trajet trajet = new Trajet();
        trajet.setId(10L);
        trajet.setPlacesProposees(1);
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(LocalDateTime.now().plusDays(1));

        PointArret a = point(100L, 0);
        PointArret b = point(101L, 1);
        PointArret c = point(102L, 2);
        trajet.setPointsArret(new ArrayList<>(List.of(a, b, c)));

        Reservation existing = new Reservation();
        existing.setId(99L);
        existing.setPassager(passager2(2L));
        existing.setStatut(StatutReservation.CONFIRMEE);
        existing.setPointMontee(a);
        existing.setPointDescente(b);
        existing.setNombrePlaces(1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.findByTrajetIdAndStatut(eq(10L), eq(StatutReservation.CONFIRMEE)))
                .thenReturn(List.of(existing));

        // A->B est déjà réservé ; selon la règle brief, la place est occupée sur tout le trajet
        ReservationRequest req = new ReservationRequest(1L, 10L, 101L, 102L, 1);
        assertThatThrownBy(() -> reservationService.createReservation(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nombre de places insuffisant");
    }

    private PointArret point(Long id, int ordre) {
        PointArret p = new PointArret();
        p.setId(id);
        p.setOrdre(ordre);
        p.setVille("V" + ordre);
        p.setHeurePassage(LocalDateTime.now().plusDays(1).plusHours(ordre));
        p.setPrixDepuisDepart(BigDecimal.valueOf(ordre * 10L));
        return p;
    }

    private Passager passager2(Long id) {
        Passager p = new Passager();
        p.setId(id);
        return p;
    }
}
