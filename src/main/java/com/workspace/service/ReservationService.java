package com.workspace.service;

import com.workspace.dto.ReservationForm;
import com.workspace.dto.ReservationResume;
import com.workspace.model.Employe;
import com.workspace.model.Equipement;
import com.workspace.model.EtatEquipement;
import com.workspace.model.MobiliteEquipement;
import com.workspace.model.Reservation;
import com.workspace.model.Salle;
import com.workspace.repository.EmployeRepository;
import com.workspace.repository.EquipementRepository;
import com.workspace.repository.ReservationRepository;
import com.workspace.repository.SalleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.time.format.DateTimeFormatter;

@Service
@Validated
@Transactional(readOnly = true)
public class ReservationService {
    private static final DateTimeFormatter DATE_AFFICHAGE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReservationRepository reservationRepository;
    private final SalleRepository salleRepository;
    private final EmployeRepository employeRepository;
    private final EquipementRepository equipementRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              SalleRepository salleRepository,
                              EmployeRepository employeRepository,
                              EquipementRepository equipementRepository) {
        this.reservationRepository = reservationRepository;
        this.salleRepository = salleRepository;
        this.employeRepository = employeRepository;
        this.equipementRepository = equipementRepository;
    }

    public List<Equipement> equipementsReservables() {
        return equipementRepository.findAllByMobiliteAndEtatNotOrderByNomAsc(
                MobiliteEquipement.MOBILE, EtatEquipement.MAINTENANCE);
    }

    public List<ReservationResume> mesReservations(String identifiantEmploye) {
        Employe employe = employeRepository.findByIdentifiantConnexionIgnoreCase(identifiantEmploye)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable."));
        return reservationRepository.findAllByEmploye_IdOrderByDateDebutDesc(employe.getId()).stream()
                .map(ReservationService::resume)
                .toList();
    }

    public ReservationResume consulter(Long id, String identifiantEmploye) {
        return resume(trouverReservation(id, identifiantEmploye));
    }

    @Transactional
    public void annuler(Long id, String identifiantEmploye) {
        reservationRepository.delete(trouverReservation(id, identifiantEmploye));
    }

    private Reservation trouverReservation(Long id, String identifiantEmploye) {
        return reservationRepository.findByIdAndEmploye_IdentifiantConnexionIgnoreCase(id, identifiantEmploye)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable."));
    }

    @Transactional
    public Long creer(@NotNull @Valid ReservationForm form, @NotNull String identifiantEmploye) {
        Salle salle = salleRepository.findById(form.getSalleId()).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Salle introuvable."));
        Employe employe = employeRepository.findByIdentifiantConnexionIgnoreCase(identifiantEmploye)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable."));

        verifierCreneau(salle, form);
        Set<Equipement> equipements = verifierEquipements(form);

        Reservation reservation = new Reservation(salle, employe, form.getDateDebut(), form.getDateFin(),
                form.getObjet(), form.getNombreParticipants(), equipements);
        return reservationRepository.save(reservation).getId();
    }

    private void verifierCreneau(Salle salle, ReservationForm form) {
        if (!form.getDateFin().isAfter(form.getDateDebut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La fin de la réservation doit être après son début.");
        }
        if (form.getNombreParticipants() > salle.getCapacite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le nombre de participants dépasse la capacité de la salle.");
        }
        if (reservationRepository.existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThan(
                salle.getId(), form.getDateFin(), form.getDateDebut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La salle est déjà réservée sur ce créneau.");
        }
    }

    private Set<Equipement> verifierEquipements(ReservationForm form) {
        Set<Long> ids = form.getEquipementIds();
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        List<Equipement> equipements = equipementRepository.findAllById(ids);
        if (equipements.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Un équipement sélectionné est introuvable.");
        }

        for (Equipement equipement : equipements) {
            if (equipement.getMobilite() != MobiliteEquipement.MOBILE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Seuls les équipements mobiles peuvent être réservés.");
            }
            if (equipement.getEtat() == EtatEquipement.MAINTENANCE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Un équipement en maintenance ne peut pas être réservé.");
            }
            if (reservationRepository.existsByEquipements_IdAndDateDebutLessThanAndDateFinGreaterThan(
                    equipement.getId(), form.getDateFin(), form.getDateDebut())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Un équipement sélectionné est déjà réservé sur ce créneau.");
            }
        }
        return new LinkedHashSet<>(equipements);
    }

    private static ReservationResume resume(Reservation reservation) {
        List<String> nomsEquipements = reservation.getEquipements().stream()
                .map(Equipement::getNom)
                .sorted()
                .toList();
        return new ReservationResume(reservation.getId(), reservation.getObjet(),
                DATE_AFFICHAGE.format(reservation.getDateDebut()), DATE_AFFICHAGE.format(reservation.getDateFin()),
                reservation.getSalle().getNom(), reservation.getNombreParticipants(), nomsEquipements);
    }
}
