package com.workspace.repository;

import com.workspace.model.Equipement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {
    List<Equipement> findAllByOrderByNomAsc();

    boolean existsByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCaseAndIdNot(String reference, Long id);

    boolean existsByTypeEquipementId(Long typeEquipementId);

    boolean existsBySalleId(Long salleId);
}
