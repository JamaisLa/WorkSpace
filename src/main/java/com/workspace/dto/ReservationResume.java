package com.workspace.dto;

import java.util.List;

public record ReservationResume(Long id, String objet, String dateDebut, String dateFin,
                                String salle, int nombreParticipants, List<String> equipements) {
}
