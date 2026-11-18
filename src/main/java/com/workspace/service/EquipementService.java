package com.workspace.service;

import com.workspace.dto.AffectationForm;
import com.workspace.dto.EquipementForm;
import com.workspace.dto.EquipementResume;
import com.workspace.model.Equipement;
import com.workspace.model.EtatEquipement;
import com.workspace.model.MobiliteEquipement;
import com.workspace.model.Salle;
import com.workspace.model.TypeEquipement;
import com.workspace.repository.EquipementRepository;
import com.workspace.repository.SalleRepository;
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
public class EquipementService {
    private final EquipementRepository equipementRepository;
    private final TypeEquipementRepository typeEquipementRepository;
    private final SalleRepository salleRepository;

    public EquipementService(EquipementRepository equipementRepository,
                             TypeEquipementRepository typeEquipementRepository,
                             SalleRepository salleRepository) {
        this.equipementRepository = equipementRepository;
        this.typeEquipementRepository = typeEquipementRepository;
        this.salleRepository = salleRepository;
    }

    public List<EquipementResume> rechercher(String recherche, EtatEquipement etat) {
        String terme = recherche == null ? "" : recherche.strip();
        return equipementRepository.rechercher(terme, etat).stream()
                .map(EquipementService::resume)
                .toList();
    }

    public EquipementResume consulter(Long id) {
        return resume(trouver(id));
    }

    public EquipementForm preparerModification(Long id) {
        Equipement equipement = trouver(id);
        EquipementForm form = new EquipementForm();
        form.setNom(equipement.getNom());
        form.setReference(equipement.getReference());
        form.setTypeEquipementId(equipement.getTypeEquipement().getId());
        form.setMobilite(equipement.getMobilite());
        form.setSalleId(equipement.getSalle() == null ? null : equipement.getSalle().getId());
        return form;
    }

    @Transactional
    public Long creer(@NotNull @Valid EquipementForm form) {
        verifierReferenceDisponible(form.getReference(), null);
        TypeEquipement type = trouverType(form.getTypeEquipementId());
        Salle salle = trouverSalleOptionnelle(form.getSalleId());
        Equipement equipement = new Equipement(form.getNom(), form.getReference(),
                form.getMobilite(), type, salle);
        equipement.setEtat(salle == null ? EtatEquipement.DISPONIBLE : EtatEquipement.AFFECTE);
        return equipementRepository.save(equipement).getId();
    }

    @Transactional
    public void modifier(Long id, @NotNull @Valid EquipementForm form) {
        Equipement equipement = trouver(id);
        verifierReferenceDisponible(form.getReference(), id);
        TypeEquipement type = trouverType(form.getTypeEquipementId());
        Salle salle = trouverSalleOptionnelle(form.getSalleId());

        boolean emplacementModifie = !memeSalle(equipement.getSalle(), salle)
                || equipement.getMobilite() != form.getMobilite();
        if (equipement.getEtat() == EtatEquipement.MAINTENANCE && emplacementModifie) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un équipement en maintenance conserve son emplacement jusqu’à sa remise en service.");
        }

        equipement.setNom(form.getNom());
        equipement.setReference(form.getReference());
        equipement.setTypeEquipement(type);
        if (equipement.getEtat() != EtatEquipement.MAINTENANCE) {
            equipement.setMobilite(form.getMobilite());
            equipement.setSalle(salle);
            equipement.setEtat(salle == null ? EtatEquipement.DISPONIBLE : EtatEquipement.AFFECTE);
        }
    }

    @Transactional
    public void affecter(Long id, @NotNull @Valid AffectationForm form) {
        Equipement equipement = trouver(id);
        if (equipement.getEtat() == EtatEquipement.MAINTENANCE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remettez l’équipement en service avant de l’affecter.");
        }
        equipement.setSalle(trouverSalle(form.getSalleId()));
        equipement.setEtat(EtatEquipement.AFFECTE);
    }

    @Transactional
    public void retirer(Long id) {
        Equipement equipement = trouver(id);
        if (equipement.getMobilite() == MobiliteEquipement.FIXE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un équipement fixe doit rester rattaché à sa salle.");
        }
        if (equipement.getEtat() == EtatEquipement.MAINTENANCE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un équipement en maintenance conserve son emplacement.");
        }
        equipement.setSalle(null);
        equipement.setEtat(EtatEquipement.DISPONIBLE);
    }

    @Transactional
    public void mettreEnMaintenance(Long id) {
        trouver(id).setEtat(EtatEquipement.MAINTENANCE);
    }

    @Transactional
    public void remettreEnService(Long id) {
        Equipement equipement = trouver(id);
        equipement.setEtat(equipement.getSalle() == null
                ? EtatEquipement.DISPONIBLE : EtatEquipement.AFFECTE);
    }

    @Transactional
    public void supprimer(Long id) {
        Equipement equipement = trouver(id);
        equipementRepository.delete(equipement);
    }

    private void verifierReferenceDisponible(String reference, Long id) {
        boolean dejaUtilisee = id == null
                ? equipementRepository.existsByReferenceIgnoreCase(reference)
                : equipementRepository.existsByReferenceIgnoreCaseAndIdNot(reference, id);
        if (dejaUtilisee) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette référence est déjà utilisée par un autre équipement.");
        }
    }

    private TypeEquipement trouverType(Long id) {
        return typeEquipementRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Type d’équipement introuvable"));
    }

    private Salle trouverSalleOptionnelle(Long id) {
        return id == null ? null : trouverSalle(id);
    }

    private Salle trouverSalle(Long id) {
        return salleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Salle introuvable"));
    }

    private Equipement trouver(Long id) {
        return equipementRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Équipement introuvable"));
    }

    private static boolean memeSalle(Salle premiere, Salle deuxieme) {
        return premiere == null ? deuxieme == null
                : deuxieme != null && premiere.getId().equals(deuxieme.getId());
    }

    private static EquipementResume resume(Equipement equipement) {
        Salle salle = equipement.getSalle();
        return new EquipementResume(equipement.getId(), equipement.getNom(), equipement.getReference(),
                equipement.getEtat(), equipement.getMobilite(), equipement.getTypeEquipement().getId(),
                equipement.getTypeEquipement().getNom(), salle == null ? null : salle.getId(),
                salle == null ? null : salle.getNom());
    }
}
