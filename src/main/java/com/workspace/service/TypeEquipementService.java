package com.workspace.service;

import com.workspace.dto.TypeEquipementResume;
import com.workspace.model.TypeEquipement;
import com.workspace.repository.TypeEquipementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    private static TypeEquipementResume resume(TypeEquipement type) {
        return new TypeEquipementResume(type.getId(), type.getNom());
    }
}
