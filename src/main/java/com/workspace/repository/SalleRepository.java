package com.workspace.repository;

import com.workspace.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByNomContainingIgnoreCaseAndCapaciteGreaterThanEqualOrderByNomAsc(String nom, int capacite);
}
