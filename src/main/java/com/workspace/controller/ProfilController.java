package com.workspace.controller;

import com.workspace.dto.ProfilForm;
import com.workspace.security.EmployeUserDetailsService;
import com.workspace.service.ProfilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
public class ProfilController {
    private final ProfilService profilService;
    private final EmployeUserDetailsService userDetailsService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public ProfilController(ProfilService profilService, EmployeUserDetailsService userDetailsService) {
        this.profilService = profilService;
        this.userDetailsService = userDetailsService;
    }

    @InitBinder("profilForm")
    void configurerFormulaire(WebDataBinder binder) {
        binder.setAllowedFields("nom", "prenom", "email", "telephone");
    }

    @GetMapping
    public String afficher(Authentication authentication, Model model) {
        model.addAttribute("profilForm", profilService.preparer(authentication.getName()));
        return "profil";
    }

    @PostMapping
    public String modifier(@Valid @ModelAttribute ProfilForm profilForm, BindingResult erreurs,
                           Authentication authentication, HttpServletRequest request,
                           HttpServletResponse response, Model model, RedirectAttributes redirect) {
        if (!erreurs.hasErrors()) {
            try {
                String nouvelIdentifiant = profilService.modifier(authentication.getName(), profilForm);
                UserDetails utilisateur = userDetailsService.loadUserByUsername(nouvelIdentifiant);
                UsernamePasswordAuthenticationToken nouvelleAuthentification = new UsernamePasswordAuthenticationToken(
                        utilisateur, null, utilisateur.getAuthorities());
                nouvelleAuthentification.setDetails(authentication.getDetails());
                SecurityContext contexte = SecurityContextHolder.createEmptyContext();
                contexte.setAuthentication(nouvelleAuthentification);
                SecurityContextHolder.setContext(contexte);
                securityContextRepository.saveContext(contexte, request, response);
                redirect.addFlashAttribute("success", "Votre profil a été mis à jour.");
                return "redirect:/profil";
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                    erreurs.rejectValue("email", "email.duplicate", exception.getReason());
                } else {
                    erreurs.reject("profil.invalid", exception.getReason());
                }
            }
        }
        model.addAttribute("profilForm", profilForm);
        return "profil";
    }
}
