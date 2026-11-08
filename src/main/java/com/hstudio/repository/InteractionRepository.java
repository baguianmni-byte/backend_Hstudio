package com.hstudio.repository;

import com.hstudio.model.Demande;
import com.hstudio.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Fournit les recherches en base pour les echanges. */
public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    /** Recherche les echanges ratt a une demande. */
    List<Interaction> findByDemande(Demande demande);
}