package com.workspace.service;

import com.workspace.dto.TypeEquipementForm;
import com.workspace.dto.TypeEquipementResume;
import com.workspace.model.TypeEquipement;
import com.workspace.repository.TypeEquipementRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class TypeEquipementService {
    private final TypeEquipementRepository repository;

    public TypeEquipementService(TypeEquipementRepository repository) {
        this.repository = repository;
    }

    public List<TypeEquipementResume> lister() {
        return repository.findAllByOrderByNomAsc().stream()
                .map(TypeEquipementService::resume)
                .toList();
    }

    public TypeEquipementResume consulter(Long id) {
        return resume(trouver(id));
    }

    public TypeEquipementForm preparerModification(Long id) {
        TypeEquipementForm form = new TypeEquipementForm();
        form.setNom(trouver(id).getNom());
        return form;
    }

    @Transactional
    public void creer(@NotNull @Valid TypeEquipementForm form) {
        repository.save(new TypeEquipement(form.getNom()));
    }

    @Transactional
    public void modifier(Long id, @NotNull @Valid TypeEquipementForm form) {
        trouver(id).setNom(form.getNom());
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(trouver(id));
    }

    private TypeEquipement trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Type d’équipement introuvable"));
    }

    private static TypeEquipementResume resume(TypeEquipement type) {
        return new TypeEquipementResume(type.getId(), type.getNom());
    }
}
