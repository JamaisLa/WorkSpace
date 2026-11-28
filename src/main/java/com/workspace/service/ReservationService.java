package com.workspace.service;

import com.workspace.dto.ReservationForm;
import com.workspace.dto.ReservationResume;
import com.workspace.dto.DisponibiliteHeure;
import com.workspace.dto.DisponibiliteSalle;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

@Service
@Validated
@Transactional(readOnly = true)
public class ReservationService {
    private static final DateTimeFormatter DATE_AFFICHAGE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HEURE_AFFICHAGE = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_HEURE_FORMULAIRE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ReservationRepository reservationRepository;
    private final SalleRepository salleRepository;
    private final EmployeRepository employeRepository;
    private final EquipementRepository equipementRepository;
    private final ZoneId zoneId;

    public ReservationService(ReservationRepository reservationRepository,
                              SalleRepository salleRepository,
                              EmployeRepository employeRepository,
                              EquipementRepository equipementRepository,
                              @Value("${workspace.time-zone:Europe/Paris}") String fuseauHoraire) {
        this.reservationRepository = reservationRepository;
        this.salleRepository = salleRepository;
        this.employeRepository = employeRepository;
        this.equipementRepository = equipementRepository;
        this.zoneId = ZoneId.of(fuseauHoraire);
    }

    public LocalDateTime maintenant() {
        return LocalDateTime.now(zoneId).truncatedTo(ChronoUnit.MINUTES);
    }

    public LocalDate aujourdHui() {
        return LocalDate.now(zoneId);
    }

    public List<DisponibiliteSalle> disponibilites(LocalDate date) {
        LocalDateTime debutJournee = date.atStartOfDay();
        LocalDateTime finJournee = date.plusDays(1).atStartOfDay();
        Map<Long, List<Reservation>> reservationsParSalle = reservationRepository
                .findAllByDateDebutLessThanAndDateFinGreaterThanOrderByDateDebutAsc(finJournee, debutJournee)
                .stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getSalle().getId()));

        return salleRepository.findByNomContainingIgnoreCaseAndCapaciteGreaterThanEqualOrderByNomAsc("", 0)
                .stream()
                .map(salle -> new DisponibiliteSalle(salle.getId(), salle.getNom(), salle.getCapacite(),
                        construireHeures(date, reservationsParSalle.getOrDefault(salle.getId(), List.of()))))
                .toList();
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
        Salle salle = salleRepository.findForReservationById(form.getSalleId()).orElseThrow(() -> new ResponseStatusException(
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
        if (form.getDateDebut().isBefore(maintenant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La date et l’heure de début doivent être dans le futur.");
        }
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

        Set<Equipement> equipements = new LinkedHashSet<>();

        // Acquérir les verrous dans le même ordre pour toutes les réservations.
        for (Long id : ids.stream().sorted().toList()) {
            Equipement equipement = equipementRepository.findForReservationById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Un équipement sélectionné est introuvable."));
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
            equipements.add(equipement);
        }
        return equipements;
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

    private List<DisponibiliteHeure> construireHeures(LocalDate date, List<Reservation> reservations) {
        LocalDateTime maintenant = maintenant();
        int premiereHeure = date.isEqual(maintenant.toLocalDate())
                ? maintenant.getHour() + (maintenant.getMinute() == 0 ? 0 : 1)
                : 0;
        return java.util.stream.IntStream.range(premiereHeure, 24)
                .mapToObj(heure -> {
                    LocalDateTime debut = date.atTime(heure, 0);
                    LocalDateTime fin = debut.plusHours(1);
                    List<Reservation> conflits = reservations.stream()
                            .filter(reservation -> reservation.getDateDebut().isBefore(fin)
                                    && reservation.getDateFin().isAfter(debut))
                            .toList();
                    boolean reservee = !conflits.isEmpty();
                    String description = conflits.stream()
                            .map(reservation -> "Réservée de " + formaterIntervalle(reservation, date))
                            .collect(Collectors.joining(" ; "));
                    String plage = conflits.stream()
                            .filter(reservation -> !reservation.getDateDebut().isBefore(debut))
                            .map(reservation -> formaterIntervalle(reservation, date))
                            .collect(Collectors.joining(" · "));
                    if (plage.isEmpty() && heure == 0 && conflits.stream()
                            .anyMatch(reservation -> reservation.getDateDebut().isBefore(debut))) {
                        plage = "En cours";
                    }
                    return new DisponibiliteHeure(HEURE_AFFICHAGE.format(debut),
                            DATE_HEURE_FORMULAIRE.format(debut), !reservee,
                            reservee, plage, description);
                })
                .toList();
    }

    private static String formaterIntervalle(Reservation reservation, LocalDate date) {
        LocalDateTime debut = reservation.getDateDebut();
        LocalDateTime fin = reservation.getDateFin();
        if (debut.toLocalDate().equals(date) && fin.toLocalDate().equals(date)) {
            return HEURE_AFFICHAGE.format(debut) + "–" + HEURE_AFFICHAGE.format(fin);
        }
        return DATE_AFFICHAGE.format(debut) + "–" + DATE_AFFICHAGE.format(fin);
    }
}
