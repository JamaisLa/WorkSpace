package com.workspace.dto;

import java.util.List;

public record DisponibiliteSalle(Long id, String nom, int capacite, List<DisponibiliteHeure> heures) {
}
