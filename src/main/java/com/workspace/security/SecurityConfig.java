package com.workspace.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/inscription", "/connexion", "/css/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/salles/nouveau", "/salles/*/modifier",
                                "/salles/*/supprimer").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/salles", "/salles/{id:[0-9]+}").authenticated()
                        .requestMatchers("/salles", "/salles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/types-equipement/nouveau",
                                "/types-equipement/*/modifier", "/types-equipement/*/supprimer").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/types-equipement").authenticated()
                        .requestMatchers("/types-equipement", "/types-equipement/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/connexion")
                        .loginProcessingUrl("/connexion")
                        .usernameParameter("identifiant")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
