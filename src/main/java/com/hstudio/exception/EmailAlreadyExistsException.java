package com.hstudio.exception;

/** Signale une adresse e-mail deja utilisee. */
public class EmailAlreadyExistsException extends RuntimeException {
    /** Cr une erreur metier correspondant au cas indique par cette exception. */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
