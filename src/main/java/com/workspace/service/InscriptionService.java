package com.workspace.service;

import com.workspace.form.InscriptionForm;
import com.workspace.model.Employe;
import com.workspace.model.RoleEmploye;
import com.workspace.repository.EmployeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class InscriptionService {
    private final EmployeRepository employeRepository;
    private final PasswordEncoder passwordEncoder;

    public InscriptionService(EmployeRepository employeRepository, PasswordEncoder passwordEncoder) {
        this.employeRepository = employeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean inscrire(InscriptionForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (employeRepository.existsByEmailIgnoreCase(email)) {
            return false;
        }

        Employe employe = new Employe(
                form.getNom().trim(),
                form.getPrenom().trim(),
                email,
                email,
                form.getTelephone() == null || form.getTelephone().isBlank()
                        ? null : form.getTelephone().trim(),
                passwordEncoder.encode(form.getMotDePasse()),
                RoleEmploye.EMPLOYE
        );
        employeRepository.save(employe);
        return true;
    }
}
