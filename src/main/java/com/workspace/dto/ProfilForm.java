package com.workspace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfilForm {
    @NotBlank(message = "Renseignez votre nom.")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
    private String nom;

    @NotBlank(message = "Renseignez votre prénom.")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
    private String prenom;

    @NotBlank(message = "Renseignez votre adresse e-mail.")
    @Email(message = "Renseignez une adresse e-mail valide.")
    @Size(max = 254, message = "L’adresse e-mail est trop longue.")
    private String email;

    @Size(max = 30, message = "Le téléphone ne peut pas dépasser 30 caractères.")
    private String telephone;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom == null ? null : nom.strip(); }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom == null ? null : prenom.strip(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT); }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) {
        this.telephone = telephone == null || telephone.isBlank() ? null : telephone.strip();
    }
}
