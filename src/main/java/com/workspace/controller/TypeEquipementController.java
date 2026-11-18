package com.workspace.controller;

import com.workspace.dto.TypeEquipementForm;
import com.workspace.service.TypeEquipementService;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/types-equipement")
public class TypeEquipementController {
    private final TypeEquipementService typeEquipementService;

    public TypeEquipementController(TypeEquipementService typeEquipementService) {
        this.typeEquipementService = typeEquipementService;
    }

    @InitBinder("typeEquipementForm")
    void configurerFormulaire(WebDataBinder binder) {
        binder.setAllowedFields("nom");
    }

    @ModelAttribute("admin")
    public boolean estAdministrateur(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authorite -> authorite.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("types", typeEquipementService.lister());
        return "types-equipement/list";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("typeEquipementForm", new TypeEquipementForm());
        preparerFormulaire(model, null);
        return "types-equipement/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute TypeEquipementForm typeEquipementForm,
                        BindingResult erreurs, Model model, RedirectAttributes redirect) {
        if (erreurs.hasErrors()) {
            preparerFormulaire(model, null);
            return "types-equipement/form";
        }
        typeEquipementService.creer(typeEquipementForm);
        redirect.addFlashAttribute("success", "La catégorie a été créée.");
        return "redirect:/types-equipement";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("typeEquipementForm", typeEquipementService.preparerModification(id));
        preparerFormulaire(model, id);
        return "types-equipement/form";
    }

    @PostMapping("/{id}")
    public String enregistrerModification(@PathVariable Long id,
                                         @Valid @ModelAttribute TypeEquipementForm typeEquipementForm,
                                         BindingResult erreurs, Model model, RedirectAttributes redirect) {
        typeEquipementService.consulter(id);
        if (erreurs.hasErrors()) {
            preparerFormulaire(model, id);
            return "types-equipement/form";
        }
        typeEquipementService.modifier(id, typeEquipementForm);
        redirect.addFlashAttribute("success", "La catégorie a été modifiée.");
        return "redirect:/types-equipement";
    }

    @GetMapping("/{id}/supprimer")
    public String confirmerSuppression(@PathVariable Long id, Model model) {
        model.addAttribute("type", typeEquipementService.consulter(id));
        return "types-equipement/delete";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirect) {
        typeEquipementService.supprimer(id);
        redirect.addFlashAttribute("success", "La catégorie a été supprimée.");
        return "redirect:/types-equipement";
    }

    private void preparerFormulaire(Model model, Long id) {
        model.addAttribute("typeId", id);
        model.addAttribute("modification", id != null);
    }
}
