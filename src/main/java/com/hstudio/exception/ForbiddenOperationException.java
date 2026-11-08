package com.hstudio.exception;

/** Signale une op interdite a un utilisateur. */
public class ForbiddenOperationException extends RuntimeException {
    /** Cr une erreur metier correspondant au cas indique par cette exception. */
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
