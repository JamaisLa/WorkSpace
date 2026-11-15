package com.workspace.repository;

import com.workspace.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
