package com.workspace.service;

import com.workspace.dto.ProfilForm;
import com.workspace.model.Employe;
import com.workspace.repository.EmployeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@Validated
@Transactional(readOnly = true)
public class ProfilService {
    private final EmployeRepository employeRepository;

    public ProfilService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    public ProfilForm preparer(@NotBlank String identifiantConnexion) {
        Employe employe = trouver(identifiantConnexion);
        ProfilForm form = new ProfilForm();
        form.setNom(employe.getNom());
        form.setPrenom(employe.getPrenom());
        form.setEmail(employe.getEmail());
        form.setTelephone(employe.getTelephone());
        return form;
    }

    @Transactional
    public String modifier(@NotBlank String identifiantConnexion, @Valid ProfilForm form) {
        Employe employe = trouver(identifiantConnexion);
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (employeRepository.existsByEmailIgnoreCaseAndIdNot(email, employe.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette adresse e-mail est déjà utilisée par un autre compte.");
        }

        employe.setNom(form.getNom());
        employe.setPrenom(form.getPrenom());
        employe.setEmail(email);
        employe.setIdentifiantConnexion(email);
        employe.setTelephone(form.getTelephone());
        return email;
    }

    private Employe trouver(String identifiantConnexion) {
        return employeRepository.findByIdentifiantConnexionIgnoreCase(identifiantConnexion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte employé introuvable."));
    }
}
