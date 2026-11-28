package com.workspace.repository;

import com.workspace.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByEmploye_IdOrderByDateDebutDesc(Long employeId);

    List<Reservation> findAllBySalle_Id(Long salleId);

    long countBySalle_Id(Long salleId);

    Optional<Reservation> findByIdAndEmploye_IdentifiantConnexionIgnoreCase(Long id, String identifiantConnexion);

    List<Reservation> findAllByOrderByDateDebutAsc();

    List<Reservation> findAllByDateDebutLessThanAndDateFinGreaterThanOrderByDateDebutAsc(
            LocalDateTime fin, LocalDateTime debut);

    boolean existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThan(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut);

    boolean existsBySalle_Id(Long salleId);

    boolean existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThanAndIdNot(
            Long salleId, LocalDateTime dateFin, LocalDateTime dateDebut, Long reservationId);

    boolean existsByEquipements_IdAndDateDebutLessThanAndDateFinGreaterThan(
            Long equipementId, LocalDateTime dateFin, LocalDateTime dateDebut);
}
