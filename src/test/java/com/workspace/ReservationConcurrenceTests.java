package com.workspace;

import com.workspace.dto.ReservationForm;
import com.workspace.model.Equipement;
import com.workspace.model.MobiliteEquipement;
import com.workspace.model.Salle;
import com.workspace.model.TypeEquipement;
import com.workspace.repository.EquipementRepository;
import com.workspace.repository.SalleRepository;
import com.workspace.repository.TypeEquipementRepository;
import com.workspace.service.ReservationService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:concurrence-tests")
class ReservationConcurrenceTests {
    @Autowired
    private ReservationService service;
    @Autowired
    private SalleRepository salles;
    @Autowired
    private EquipementRepository equipements;
    @Autowired
    private TypeEquipementRepository types;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void uneSeuleDemandeEstAccepteePourUneRessourcePartagee(boolean memeSalle) throws Exception {
        Salle premiereSalle = salles.save(new Salle("Salle A", 8, "Étage 1"));
        Salle secondeSalle = memeSalle ? premiereSalle : salles.save(new Salle("Salle B", 8, "Étage 1"));
        Set<Long> materiel = Set.of();
        if (!memeSalle) {
            TypeEquipement type = types.save(new TypeEquipement("Vidéoprojecteur"));
            Equipement projecteur = equipements.save(new Equipement("Projecteur mobile", "CONC-01",
                    MobiliteEquipement.MOBILE, type, null));
            materiel = Set.of(projecteur.getId());
        }
        ReservationForm premiere = formulaire(premiereSalle, materiel);
        ReservationForm seconde = formulaire(secondeSalle, materiel);
        CyclicBarrier depart = new CyclicBarrier(2);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var resultatPremier = executor.submit(() -> reserver(premiere, depart));
            var resultatSecond = executor.submit(() -> reserver(seconde, depart));
            assertThat(new String[]{resultatPremier.get(10, TimeUnit.SECONDS),
                    resultatSecond.get(10, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder("acceptée", memeSalle
                            ? "La salle est déjà réservée sur ce créneau."
                            : "Un équipement sélectionné est déjà réservé sur ce créneau.");
        }
    }

    private String reserver(ReservationForm formulaire, CyclicBarrier depart) throws Exception {
        depart.await(5, TimeUnit.SECONDS);
        try {
            service.creer(formulaire, "admin");
            return "acceptée";
        } catch (ResponseStatusException exception) {
            return exception.getReason();
        }
    }

    private ReservationForm formulaire(Salle salle, Set<Long> materiel) {
        LocalDateTime debut = LocalDateTime.of(2027, 3, 1, 10, 0);
        ReservationForm formulaire = new ReservationForm();
        formulaire.setSalleId(salle.getId());
        formulaire.setObjet("Réunion");
        formulaire.setDateDebut(debut);
        formulaire.setDateFin(debut.plusHours(1));
        formulaire.setNombreParticipants(2);
        formulaire.setEquipementIds(materiel);
        return formulaire;
    }
}
