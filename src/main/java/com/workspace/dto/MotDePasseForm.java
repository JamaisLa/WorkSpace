package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MotDePasseForm {
    @NotBlank(message = "Renseignez votre mot de passe actuel.")
    private String motDePasseActuel;

    @NotBlank(message = "Renseignez un nouveau mot de passe.")
    @Size(min = 8, max = 100, message = "Le nouveau mot de passe doit contenir entre 8 et 100 caractères.")
    private String nouveauMotDePasse;

    @NotBlank(message = "Confirmez votre nouveau mot de passe.")
    private String confirmation;

    public String getMotDePasseActuel() { return motDePasseActuel; }
    public void setMotDePasseActuel(String motDePasseActuel) { this.motDePasseActuel = motDePasseActuel; }
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String nouveauMotDePasse) { this.nouveauMotDePasse = nouveauMotDePasse; }
    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String confirmation) { this.confirmation = confirmation; }
}
