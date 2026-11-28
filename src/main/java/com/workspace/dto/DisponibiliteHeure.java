package com.workspace.dto;

public record DisponibiliteHeure(String heure, String dateDebut, boolean disponible,
                                 boolean reservee, String plageReservee,
                                 String description) {
}
