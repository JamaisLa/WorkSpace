package com.workspace.dto;

import com.workspace.model.EtatEquipement;
import com.workspace.model.MobiliteEquipement;

public record EquipementResume(Long id, String nom, String reference, EtatEquipement etat,
                               MobiliteEquipement mobilite, Long typeEquipementId,
                               String typeEquipementNom, Long salleId, String salleNom) {
}
