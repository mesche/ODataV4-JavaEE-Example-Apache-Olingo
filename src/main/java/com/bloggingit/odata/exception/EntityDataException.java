package com.bloggingit.odata.exception;

/**
 * Exception thrown when an error occurs while accessing a data source.
 */
public class EntityDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public EntityDataException(String message) {
        super(message);
    }

    public EntityDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
