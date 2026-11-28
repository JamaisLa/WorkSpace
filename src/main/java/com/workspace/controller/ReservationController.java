package com.workspace.controller;

import com.workspace.dto.ReservationForm;
import com.workspace.dto.SalleResume;
import com.workspace.service.ReservationService;
import com.workspace.service.SalleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final SalleService salleService;

    public ReservationController(ReservationService reservationService, SalleService salleService) {
        this.reservationService = reservationService;
        this.salleService = salleService;
    }

    @InitBinder("reservationForm")
    void configurerFormulaire(WebDataBinder binder) {
        binder.setAllowedFields("salleId", "objet", "dateDebut", "dateFin", "nombreParticipants", "equipementIds");
    }

    @GetMapping
    public String lister(Authentication authentication, Model model) {
        model.addAttribute("reservations", reservationService.mesReservations(authentication.getName()));
        return "reservations/list";
    }

    @GetMapping("/{id:[0-9]+}")
    public String consulter(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("reservation", reservationService.consulter(id, authentication.getName()));
        return "reservations/detail";
    }

    @GetMapping("/{id:[0-9]+}/annuler")
    public String confirmerAnnulation(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("reservation", reservationService.consulter(id, authentication.getName()));
        return "reservations/cancel";
    }

    @PostMapping("/{id:[0-9]+}/annuler")
    public String annuler(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
        reservationService.annuler(id, authentication.getName());
        redirect.addFlashAttribute("success", "La réservation a été annulée.");
        return "redirect:/reservations";
    }

    @GetMapping("/nouvelle")
    public String nouvelle() {
        return "redirect:/reservations/disponibilites";
    }

    @GetMapping("/disponibilites")
    public String disponibilites(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate aujourdHui = reservationService.aujourdHui();
        LocalDate dateAffichee = date == null ? aujourdHui : date;
        if (dateAffichee.isBefore(aujourdHui)) {
            dateAffichee = aujourdHui;
            model.addAttribute("error", "Choisissez aujourd’hui ou une date future.");
        }
        model.addAttribute("date", dateAffichee);
        model.addAttribute("dateLibelle", DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
                .format(dateAffichee));
        model.addAttribute("dateMinimum", aujourdHui);
        model.addAttribute("datePrecedente", dateAffichee.minusDays(1));
        model.addAttribute("dateSuivante", dateAffichee.plusDays(1));
        model.addAttribute("salles", reservationService.disponibilites(dateAffichee));
        return "reservations/disponibilites";
    }

    @GetMapping("/nouvelle/formulaire")
    public String formulaire(@RequestParam(required = false) Long salleId,
                             @RequestParam(required = false)
                             @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dateDebut,
                             @RequestParam(required = false)
                             @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dateFin,
                             Model model) {
        ReservationForm form = new ReservationForm();
        form.setSalleId(salleId);
        form.setDateDebut(dateDebut);
        form.setDateFin(dateFin == null && dateDebut != null ? dateDebut.plusHours(1) : dateFin);
        model.addAttribute("reservationForm", form);
        preparerFormulaire(model, dateDebut == null ? reservationService.aujourdHui() : dateDebut.toLocalDate());
        preparerCreneauChoisi(model, salleId, dateDebut, form.getDateFin());
        return "reservations/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute ReservationForm reservationForm, BindingResult erreurs,
                        Authentication authentication, Model model, RedirectAttributes redirect) {
        if (!erreurs.hasErrors()) {
            try {
                reservationService.creer(reservationForm, authentication.getName());
                redirect.addFlashAttribute("success", "La réservation a été enregistrée.");
                return "redirect:/reservations";
            } catch (ResponseStatusException exception) {
                erreurs.reject("reservation.invalid", exception.getReason());
            }
        }
        LocalDate dateRetour = reservationForm.getDateDebut() == null
                ? reservationService.aujourdHui() : reservationForm.getDateDebut().toLocalDate();
        preparerFormulaire(model, dateRetour);
        preparerCreneauChoisi(model, reservationForm.getSalleId(), reservationForm.getDateDebut(), reservationForm.getDateFin());
        return "reservations/form";
    }

    private void preparerFormulaire(Model model, LocalDate dateRetour) {
        model.addAttribute("salles", salleService.rechercher("", 0));
        model.addAttribute("equipements", reservationService.equipementsReservables());
        model.addAttribute("dateMinimum", reservationService.aujourdHui());
        model.addAttribute("dateHeureMinimum", reservationService.maintenant()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        model.addAttribute("dateRetour", dateRetour);
    }

    private void preparerCreneauChoisi(Model model, Long salleId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        SalleResume salle = salleId == null ? null : salleService.rechercher("", 0).stream()
                .filter(candidate -> candidate.id().equals(salleId))
                .findFirst().orElse(null);
        boolean choisi = salle != null && dateDebut != null && dateFin != null && dateFin.isAfter(dateDebut);
        model.addAttribute("creneauChoisi", choisi);
        if (choisi) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH);
            model.addAttribute("salleChoisie", salle);
            model.addAttribute("debutChoisi", format.format(dateDebut));
            model.addAttribute("finChoisie", format.format(dateFin));
        }
    }
}
