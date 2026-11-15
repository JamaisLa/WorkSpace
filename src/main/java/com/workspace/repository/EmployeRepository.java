package com.workspace.repository;

import com.workspace.model.Employe;
import com.workspace.model.RoleEmploye;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Employe> findByIdentifiantConnexionIgnoreCase(String identifiantConnexion);

    boolean existsByRole(RoleEmploye role);
}
