package com.workspace.repository;

import com.workspace.model.Equipement;
import com.workspace.model.EtatEquipement;
import com.workspace.model.MobiliteEquipement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Equipement> findForReservationById(Long id);

    @EntityGraph(attributePaths = {"typeEquipement", "salle"})
    @Query("""
            select e from Equipement e
            where (:etat is null or e.etat = :etat)
              and (locate(lower(:q), lower(e.nom)) > 0
                   or locate(lower(:q), lower(e.reference)) > 0)
            order by e.nom, e.reference
            """)
    List<Equipement> rechercher(@Param("q") String recherche, @Param("etat") EtatEquipement etat);

    boolean existsByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCaseAndIdNot(String reference, Long id);

    boolean existsByTypeEquipementId(Long typeEquipementId);

    boolean existsBySalleId(Long salleId);

    List<Equipement> findAllBySalleId(Long salleId);

    long countBySalleId(Long salleId);

    List<Equipement> findAllByMobiliteAndEtatNotOrderByNomAsc(
            MobiliteEquipement mobilite, EtatEquipement etat);
}
