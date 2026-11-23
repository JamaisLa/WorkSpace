package com.workspace;

import com.workspace.model.Employe;
import com.workspace.model.Equipement;
import com.workspace.model.MobiliteEquipement;
import com.workspace.model.Reservation;
import com.workspace.model.RoleEmploye;
import com.workspace.model.Salle;
import com.workspace.model.TypeEquipement;
import com.workspace.repository.EmployeRepository;
import com.workspace.repository.EquipementRepository;
import com.workspace.repository.ReservationRepository;
import com.workspace.repository.SalleRepository;
import com.workspace.repository.TypeEquipementRepository;
import com.workspace.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:annulation-tests")
@Transactional
class ReservationAnnulationTests {
    @Autowired
    private ReservationService service;
    @Autowired
    private ReservationRepository reservations;
    @Autowired
    private SalleRepository salles;
    @Autowired
    private EmployeRepository employes;
    @Autowired
    private EquipementRepository equipements;
    @Autowired
    private TypeEquipementRepository types;

    @Test
    void seulLeProprietairePeutAnnulerEtLibererLeCreneau() {
        Employe proprietaire = employes.save(new Employe("Dupont", "Camille", "camille@example.test",
                "camille", "0600000000", "hash", RoleEmploye.EMPLOYE));
        employes.save(new Employe("Martin", "Lou", "lou@example.test",
                "lou", "0600000001", "hash", RoleEmploye.EMPLOYE));
        Salle salle = salles.save(new Salle("Salle de réunion", 8, "Étage 1"));
        TypeEquipement type = types.save(new TypeEquipement("Vidéoprojecteur"));
        Equipement equipement = equipements.save(new Equipement("Projecteur mobile", "PROJ-01",
                MobiliteEquipement.MOBILE, type, null));
        LocalDateTime debut = LocalDateTime.of(2027, 3, 1, 10, 0);
        LocalDateTime fin = debut.plusHours(1);
        Long id = reservations.saveAndFlush(new Reservation(salle, proprietaire, debut, fin,
                "Réunion", 2, Set.of(equipement))).getId();

        assertThatThrownBy(() -> service.annuler(id, "lou"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        erreur -> assertThat(erreur.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        assertThat(reservations.existsById(id)).isTrue();

        service.annuler(id, "camille");
        reservations.flush();

        assertThat(reservations.existsById(id)).isFalse();
        assertThat(reservations.existsBySalle_IdAndDateDebutLessThanAndDateFinGreaterThan(
                salle.getId(), fin, debut)).isFalse();
        assertThat(reservations.existsByEquipements_IdAndDateDebutLessThanAndDateFinGreaterThan(
                equipement.getId(), fin, debut)).isFalse();
    }
}
