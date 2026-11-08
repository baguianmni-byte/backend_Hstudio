package com.hstudio.repository;

import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;
import com.hstudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.UUID;
import java.util.Collection;

/** Fournit les recherches en base pour les demandes commerciales. */
public interface DemandeRepository extends JpaRepository<Demande, UUID>, JpaSpecificationExecutor<Demande> {
    
    /** Recherche les demandes attribuees au commercial indique. */
    List<Demande> findByAssignedCommercial(User commercial);
    
    /** Recherche les demandes ayant le statut indique. */
    List<Demande> findByStatus(DemandeStatus status);
    
    /** Recherche les demandes associées au pôle de service indiqué. */
    List<Demande> findByServicePole(String servicePole);

    /** Compte les demandes actives du commercial, en ignorant les statuts terminaux. */
    long countByAssignedCommercialAndStatusNotIn(User commercial, Collection<DemandeStatus> statuses);

    /** Compte les demandes du commercial qui ont le statut indiqué. */
    long countByAssignedCommercialAndStatus(User commercial, DemandeStatus status);
}