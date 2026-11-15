package com.workspace.security;

import com.workspace.model.Employe;
import com.workspace.repository.EmployeRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeUserDetailsService implements UserDetailsService {
    private final EmployeRepository employeRepository;

    public EmployeUserDetailsService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        Employe employe = employeRepository.findByIdentifiantConnexionIgnoreCase(identifiant)
                .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable"));

        return User.withUsername(employe.getIdentifiantConnexion())
                .password(employe.getMotDePasseHash())
                .roles(employe.getRole().name())
                .build();
    }
}
