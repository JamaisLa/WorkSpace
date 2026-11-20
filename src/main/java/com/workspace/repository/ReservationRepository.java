package com.workspace.repository;

import com.workspace.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByEmployeIdOrderByDateDebutDesc(Long employeId);

    List<Reservation> findAllByOrderByDateDebutAsc();

    boolean existsBySalleIdAndDateDebutLessThanAndDateFinGreaterThan(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut);

    boolean existsBySalleIdAndDateDebutLessThanAndDateFinGreaterThanAndIdNot(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut, Long reservationId);
}
