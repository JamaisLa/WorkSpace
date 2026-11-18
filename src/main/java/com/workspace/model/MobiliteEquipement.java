package com.workspace.model;

public enum MobiliteEquipement {
    FIXE("Fixe"),
    MOBILE("Mobile");

    private final String libelle;

    MobiliteEquipement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
