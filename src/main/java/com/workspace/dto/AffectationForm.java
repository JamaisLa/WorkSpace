package com.workspace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AffectationForm {
    @NotNull(message = "Choisissez une salle.")
    @Positive(message = "Choisissez une salle valide.")
    private Long salleId;

    public Long getSalleId() {
        return salleId;
    }

    public void setSalleId(Long salleId) {
        this.salleId = salleId;
    }
}
