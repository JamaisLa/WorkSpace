package com.workspace.security;

import com.workspace.model.Employe;
import com.workspace.model.RoleEmploye;
import com.workspace.repository.EmployeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final EmployeRepository employeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(EmployeRepository employeRepository, PasswordEncoder passwordEncoder) {
        this.employeRepository = employeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!employeRepository.existsByRole(RoleEmploye.ADMIN)) {
            employeRepository.save(new Employe(
                    "Administrateur",
                    "WorkSpace",
                    "admin@workspace.invalid",
                    "admin",
                    null,
                    passwordEncoder.encode("admin"),
                    RoleEmploye.ADMIN
            ));
        }
    }
}
