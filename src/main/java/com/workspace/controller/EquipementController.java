package com.workspace.controller;

import com.workspace.dto.AffectationForm;
import com.workspace.dto.EquipementForm;
import com.workspace.dto.EquipementResume;
import com.workspace.model.EtatEquipement;
import com.workspace.model.MobiliteEquipement;
import com.workspace.service.EquipementService;
import com.workspace.service.SalleService;
import com.workspace.service.TypeEquipementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/equipements")
public class EquipementController {
    private final EquipementService equipementService;
    private final TypeEquipementService typeEquipementService;
    private final SalleService salleService;

    public EquipementController(EquipementService equipementService,
                                TypeEquipementService typeEquipementService,
                                SalleService salleService) {
        this.equipementService = equipementService;
        this.typeEquipementService = typeEquipementService;
        this.salleService = salleService;
    }

    @InitBinder("equipementForm")
    void configurerEquipement(WebDataBinder binder) {
        binder.setAllowedFields("nom", "reference", "typeEquipementId", "mobilite", "salleId");
    }

    @InitBinder("affectationForm")
    void configurerAffectation(WebDataBinder binder) {
        binder.setAllowedFields("salleId");
    }

    @ModelAttribute("admin")
    public boolean estAdministrateur(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authorite -> authorite.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public String lister(@RequestParam(defaultValue = "") String q,
                         @RequestParam(required = false) EtatEquipement etat, Model model) {
        model.addAttribute("equipements", equipementService.rechercher(q, etat));
        model.addAttribute("q", q.strip());
        model.addAttribute("etat", etat);
        model.addAttribute("etats", EtatEquipement.values());
        return "equipements/list";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("equipementForm", new EquipementForm());
        preparerFormulaire(model, null);
        return "equipements/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute EquipementForm equipementForm,
                        BindingResult erreurs, Model model, RedirectAttributes redirect) {
        Long id = null;
        if (!erreurs.hasErrors()) {
            try {
                id = equipementService.creer(equipementForm);
            } catch (ResponseStatusException exception) {
                ajouterErreur(erreurs, exception);
            }
        }
        if (erreurs.hasErrors()) {
            preparerFormulaire(model, null);
            return "equipements/form";
        }
        redirect.addFlashAttribute("success", "L’équipement a été créé.");
        return "redirect:/equipements/" + id;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        preparerDetails(model, id);
        return "equipements/detail";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("equipementForm", equipementService.preparerModification(id));
        preparerFormulaire(model, id);
        return "equipements/form";
    }

    @PostMapping("/{id}")
    public String enregistrerModification(@PathVariable Long id,
                                         @Valid @ModelAttribute EquipementForm equipementForm,
                                         BindingResult erreurs, Model model, RedirectAttributes redirect) {
        equipementService.consulter(id);
        if (!erreurs.hasErrors()) {
            try {
                equipementService.modifier(id, equipementForm);
            } catch (ResponseStatusException exception) {
                ajouterErreur(erreurs, exception);
            }
        }
        if (erreurs.hasErrors()) {
            preparerFormulaire(model, id);
            return "equipements/form";
        }
        redirect.addFlashAttribute("success", "L’équipement a été modifié.");
        return "redirect:/equipements/" + id;
    }

    @PostMapping("/{id}/affecter")
    public String affecter(@PathVariable Long id, @Valid @ModelAttribute AffectationForm affectationForm,
                           BindingResult erreurs, Model model, RedirectAttributes redirect) {
        if (erreurs.hasErrors()) {
            preparerDetails(model, id);
            return "equipements/detail";
        }
        try {
            equipementService.affecter(id, affectationForm);
            redirect.addFlashAttribute("success", "L’affectation a été enregistrée.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        }
        return "redirect:/equipements/" + id;
    }

    @PostMapping("/{id}/retirer")
    public String retirer(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            equipementService.retirer(id);
            redirect.addFlashAttribute("success", "L’équipement a été retiré de la salle.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        }
        return "redirect:/equipements/" + id;
    }

    @PostMapping("/{id}/maintenance")
    public String maintenance(@PathVariable Long id, RedirectAttributes redirect) {
        equipementService.mettreEnMaintenance(id);
        redirect.addFlashAttribute("success", "L’équipement est maintenant en maintenance.");
        return "redirect:/equipements/" + id;
    }

    @PostMapping("/{id}/remettre-en-service")
    public String remettreEnService(@PathVariable Long id, RedirectAttributes redirect) {
        equipementService.remettreEnService(id);
        redirect.addFlashAttribute("success", "L’équipement a été remis en service.");
        return "redirect:/equipements/" + id;
    }

    @GetMapping("/{id}/supprimer")
    public String confirmerSuppression(@PathVariable Long id, Model model) {
        model.addAttribute("equipement", equipementService.consulter(id));
        return "equipements/delete";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirect) {
        equipementService.supprimer(id);
        redirect.addFlashAttribute("success", "L’équipement a été supprimé.");
        return "redirect:/equipements";
    }

    private void preparerFormulaire(Model model, Long id) {
        model.addAttribute("equipementId", id);
        model.addAttribute("modification", id != null);
        model.addAttribute("types", typeEquipementService.lister());
        model.addAttribute("salles", salleService.rechercher("", 0));
        model.addAttribute("mobilites", MobiliteEquipement.values());
    }

    private void preparerDetails(Model model, Long id) {
        EquipementResume equipement = equipementService.consulter(id);
        model.addAttribute("equipement", equipement);
        model.addAttribute("salles", salleService.rechercher("", 0));
        if (!model.containsAttribute("affectationForm")) {
            AffectationForm form = new AffectationForm();
            form.setSalleId(equipement.salleId());
            model.addAttribute("affectationForm", form);
        }
    }

    private void ajouterErreur(BindingResult erreurs, ResponseStatusException exception) {
        String message = exception.getReason() == null ? "Impossible d’enregistrer l’équipement." : exception.getReason();
        if (exception.getStatusCode() == HttpStatus.CONFLICT && message.contains("référence")) {
            erreurs.rejectValue("reference", "reference.duplicate", message);
        } else {
            erreurs.reject("equipement.invalid", message);
        }
    }
}
