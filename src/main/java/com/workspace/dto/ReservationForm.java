package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class ReservationForm {
    @NotNull(message = "Choisissez une salle.")
    @Positive(message = "Choisissez une salle valide.")
    private Long salleId;

    @NotBlank(message = "Renseignez l’objet de la réservation.")
    @Size(max = 200, message = "L’objet ne peut pas dépasser 200 caractères.")
    private String objet;

    @NotNull(message = "Renseignez le début de la réservation.")
    private LocalDateTime dateDebut;

    @NotNull(message = "Renseignez la fin de la réservation.")
    private LocalDateTime dateFin;

    @NotNull(message = "Renseignez le nombre de participants.")
    @Positive(message = "Le nombre de participants doit être positif.")
    private Integer nombreParticipants;

    private Set<@NotNull @Positive Long> equipementIds = new LinkedHashSet<>();

    public Long getSalleId() { return salleId; }
    public void setSalleId(Long salleId) { this.salleId = salleId; }
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet == null ? null : objet.strip(); }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public Integer getNombreParticipants() { return nombreParticipants; }
    public void setNombreParticipants(Integer nombreParticipants) { this.nombreParticipants = nombreParticipants; }
    public Set<Long> getEquipementIds() { return equipementIds; }
    public void setEquipementIds(Set<Long> equipementIds) {
        this.equipementIds = equipementIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(equipementIds);
    }
}
