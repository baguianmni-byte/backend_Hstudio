package com.hstudio.model;

/** Liste des états possibles du traitement une demande. */
public enum DemandeStatus {
    NOUVELLE_DEMANDE,
    A_PRENDRE_EN_CHARGE,
    EN_COURS_DE_TRAITEMENT,
    EN_ATTENTE_CLIENT,
    PROPOSITION_COMMERCIALE_ENVOYEE,
    GAGNEE,
    PERDUE,
    CLOTUREE_SANS_SUITE
}