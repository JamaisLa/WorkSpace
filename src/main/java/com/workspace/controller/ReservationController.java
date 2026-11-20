package com.workspace.controller;

import com.workspace.dto.ReservationForm;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/nouvelle")
    public String nouvelle(Model model) {
        model.addAttribute("reservationForm", new ReservationForm());
        preparerFormulaire(model);
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
        preparerFormulaire(model);
        return "reservations/form";
    }

    private void preparerFormulaire(Model model) {
        model.addAttribute("salles", salleService.rechercher("", 0));
        model.addAttribute("equipements", reservationService.equipementsReservables());
    }
}
