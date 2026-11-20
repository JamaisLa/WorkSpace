package com.workspace.repository;

import com.workspace.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByEmploye_IdOrderByDateDebutDesc(Long employeId);

    List<Reservation> findAllByOrderByDateDebutAsc();

    boolean existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThan(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut);

    boolean existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThanAndIdNot(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut, Long reservationId);

    boolean existsByEquipements_IdAndDateDebutLessThanAndDateFinGreaterThan(
            Long equipementId, LocalDateTime dateFin, LocalDateTime dateDebut);
}
