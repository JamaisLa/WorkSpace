package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SalleForm {

    @NotBlank(message = "Renseignez le nom de la salle.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String nom;

    @NotNull(message = "Renseignez la capacité de la salle.")
    @Positive(message = "La capacité doit être d'au moins une personne.")
    private Integer capacite;

    @NotBlank(message = "Renseignez la localisation de la salle.")
    @Size(max = 200, message = "La localisation ne peut pas dépasser 200 caractères.")
    private String localisation;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom == null ? null : nom.strip();
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation == null ? null : localisation.strip();
    }
}
