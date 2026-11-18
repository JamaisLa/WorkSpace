package com.workspace.model;

public enum EtatEquipement {
    DISPONIBLE("Disponible"),
    AFFECTE("Affecté"),
    MAINTENANCE("En maintenance");

    private final String libelle;

    EtatEquipement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
