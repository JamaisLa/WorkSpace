package com.workspace.repository;

import com.workspace.model.TypeEquipement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeEquipementRepository extends JpaRepository<TypeEquipement, Long> {
    List<TypeEquipement> findAllByOrderByNomAsc();

    boolean existsByNomIgnoreCase(String nom);
}
