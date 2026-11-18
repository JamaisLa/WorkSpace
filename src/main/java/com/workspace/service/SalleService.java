package com.workspace.service;

import com.workspace.dto.SalleForm;
import com.workspace.dto.SalleResume;
import com.workspace.model.Salle;
import com.workspace.repository.SalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class SalleService {

    private final SalleRepository salleRepository;

    public SalleService(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public List<SalleResume> rechercher(String recherche, int capaciteMin) {
        String nom = recherche == null ? "" : recherche.strip();
        return salleRepository
                .findByNomContainingIgnoreCaseAndCapaciteGreaterThanEqualOrderByNomAsc(nom, Math.max(0, capaciteMin))
                .stream()
                .map(SalleService::resume)
                .toList();
    }

    public SalleResume consulter(Long id) {
        return resume(trouver(id));
    }

    public SalleForm preparerModification(Long id) {
        Salle salle = trouver(id);
        SalleForm form = new SalleForm();
        form.setNom(salle.getNom());
        form.setCapacite(salle.getCapacite());
        form.setLocalisation(salle.getLocalisation());
        return form;
    }

    @Transactional
    public Long creer(@NotNull @Valid SalleForm form) {
        Salle salle = new Salle(form.getNom(), form.getCapacite(), form.getLocalisation());
        return salleRepository.save(salle).getId();
    }

    @Transactional
    public void modifier(Long id, @NotNull @Valid SalleForm form) {
        Salle salle = trouver(id);
        salle.setNom(form.getNom());
        salle.setCapacite(form.getCapacite());
        salle.setLocalisation(form.getLocalisation());
    }

    @Transactional
    public void supprimer(Long id) {
        salleRepository.delete(trouver(id));
    }

    private static SalleResume resume(Salle salle) {
        return new SalleResume(salle.getId(), salle.getNom(), salle.getCapacite(), salle.getLocalisation());
    }

    private Salle trouver(Long id) {
        return salleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle introuvable"));
    }
}
