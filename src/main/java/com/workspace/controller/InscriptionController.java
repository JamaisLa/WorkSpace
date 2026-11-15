package com.workspace.controller;

import com.workspace.form.InscriptionForm;
import com.workspace.service.InscriptionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InscriptionController {
    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    @GetMapping("/inscription")
    public String afficherFormulaire(Model model) {
        model.addAttribute("inscription", new InscriptionForm());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@Valid @ModelAttribute("inscription") InscriptionForm form,
                           BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("motDePasse")
                && !form.getMotDePasse().equals(form.getConfirmation())) {
            bindingResult.rejectValue("confirmation", "confirmation.differente",
                    "Les mots de passe ne correspondent pas.");
        }

        if (bindingResult.hasErrors()) {
            return "inscription";
        }

        if (!inscriptionService.inscrire(form)) {
            bindingResult.rejectValue("email", "email.dejaUtilise",
                    "Un compte existe déjà avec cette adresse email.");
            return "inscription";
        }

        return "redirect:/connexion?inscription";
    }
}
