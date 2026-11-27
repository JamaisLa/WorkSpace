package com.workspace.repository;

import com.workspace.model.Salle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

public interface SalleRepository extends JpaRepository<Salle, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Salle> findForReservationById(Long id);

    List<Salle> findByNomContainingIgnoreCaseAndCapaciteGreaterThanEqualOrderByNomAsc(String nom, int capacite);
}
