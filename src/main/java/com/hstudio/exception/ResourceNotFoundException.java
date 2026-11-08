package com.hstudio.exception;

/** Signale une ressource demandee qui est absente. */
public class ResourceNotFoundException extends RuntimeException {
    /** Cr une erreur metier correspondant au cas indique par cette exception. */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
