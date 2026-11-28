package com.workspace.controller;

import com.workspace.dto.SalleForm;
import com.workspace.service.SalleService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/salles")
public class SalleController {

    private final SalleService salleService;

    public SalleController(SalleService salleService) {
        this.salleService = salleService;
    }

    @InitBinder("salleForm")
    void configurerFormulaire(WebDataBinder binder) {
        binder.setAllowedFields("nom", "capacite", "localisation");
    }

    @ModelAttribute("admin")
    public boolean estAdministrateur(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autorite -> autorite.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public String lister(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int capaciteMin, Model model) {
        model.addAttribute("salles", salleService.rechercher(q, capaciteMin));
        model.addAttribute("q", q.strip());
        model.addAttribute("capaciteMin", Math.max(0, capaciteMin));
        return "salles/list";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("salleForm", new SalleForm());
        preparerFormulaire(model, null);
        return "salles/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute SalleForm salleForm, BindingResult erreurs,
                        Model model, RedirectAttributes redirect) {
        if (erreurs.hasErrors()) {
            preparerFormulaire(model, null);
            return "salles/form";
        }
        Long id = salleService.creer(salleForm);
        redirect.addFlashAttribute("success", "La salle a été créée.");
        return "redirect:/salles/" + id;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("salle", salleService.consulter(id));
        return "salles/detail";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("salleForm", salleService.preparerModification(id));
        preparerFormulaire(model, id);
        return "salles/form";
    }

    @PostMapping("/{id}")
    public String enregistrerModification(@PathVariable Long id, @Valid @ModelAttribute SalleForm salleForm,
                                         BindingResult erreurs, Model model, RedirectAttributes redirect) {
        if (erreurs.hasErrors()) {
            salleService.consulter(id);
            preparerFormulaire(model, id);
            return "salles/form";
        }
        salleService.modifier(id, salleForm);
        redirect.addFlashAttribute("success", "Les modifications ont été enregistrées.");
        return "redirect:/salles/" + id;
    }

    @GetMapping("/{id}/supprimer")
    public String confirmerSuppression(@PathVariable Long id, Model model) {
        model.addAttribute("salle", salleService.consulter(id));
        model.addAttribute("nombreReservations", salleService.nombreReservations(id));
        model.addAttribute("nombreEquipements", salleService.nombreEquipements(id));
        return "salles/delete";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            long reservationsSupprimees = salleService.supprimer(id);
            String message = reservationsSupprimees == 1
                    ? "La salle et sa réservation associée ont été supprimées."
                    : reservationsSupprimees > 1
                    ? "La salle et ses " + reservationsSupprimees + " réservations associées ont été supprimées."
                    : "La salle a été supprimée.";
            redirect.addFlashAttribute("success", message
                    + " Les équipements liés sont conservés dans l’inventaire sans salle associée.");
            return "redirect:/salles";
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        } catch (DataIntegrityViolationException exception) {
            redirect.addFlashAttribute("error",
                    "La suppression a échoué à cause d’un lien encore utilisé. Aucune donnée n’a été supprimée.");
        }
        return "redirect:/salles/" + id + "/supprimer";
    }

    private void preparerFormulaire(Model model, Long id) {
        model.addAttribute("salleId", id);
        model.addAttribute("modification", id != null);
    }
}
