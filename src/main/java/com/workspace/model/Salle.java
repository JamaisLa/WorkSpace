package com.workspace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    @Positive
    @Column(nullable = false)
    private int capacite;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String localisation;

    protected Salle() {
    }

    public Salle(String nom, int capacite, String localisation) {
        this.nom = nom;
        this.capacite = capacite;
        this.localisation = localisation;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
}
