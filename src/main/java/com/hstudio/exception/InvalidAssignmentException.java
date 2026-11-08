package com.hstudio.exception;

/** Signale une affectation qui ne respecte pas les regles metier. */
public class InvalidAssignmentException extends RuntimeException {
    /** Cr une erreur metier correspondant au cas indique par cette exception. */
    public InvalidAssignmentException(String message) {
        super(message);
    }
}
