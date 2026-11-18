package com.workspace.dto;

import java.util.Locale;

import com.workspace.model.MobiliteEquipement;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class EquipementForm {
    @NotBlank(message = "Renseignez le nom de l’équipement.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String nom;

    @NotBlank(message = "Renseignez une référence unique.")
    @Size(max = 100, message = "La référence ne peut pas dépasser 100 caractères.")
    private String reference;

    @NotNull(message = "Choisissez une catégorie.")
    @Positive(message = "Choisissez une catégorie valide.")
    private Long typeEquipementId;

    @NotNull(message = "Choisissez si l’équipement est fixe ou mobile.")
    private MobiliteEquipement mobilite;

    @Positive(message = "Choisissez une salle valide.")
    private Long salleId;

    @AssertTrue(message = "Un équipement fixe doit être rattaché à une salle.")
    public boolean isSalleValide() {
        return mobilite != MobiliteEquipement.FIXE || salleId != null;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom == null ? null : nom.strip(); }
    public String getReference() { return reference; }
    public void setReference(String reference) {
        this.reference = reference == null ? null : reference.strip().toUpperCase(Locale.ROOT);
    }
    public Long getTypeEquipementId() { return typeEquipementId; }
    public void setTypeEquipementId(Long typeEquipementId) { this.typeEquipementId = typeEquipementId; }
    public MobiliteEquipement getMobilite() { return mobilite; }
    public void setMobilite(MobiliteEquipement mobilite) { this.mobilite = mobilite; }
    public Long getSalleId() { return salleId; }
    public void setSalleId(Long salleId) { this.salleId = salleId; }
}
