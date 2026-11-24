package com.example.ms_reservas.exception;

// Se transforma en un error 404 en GlobalExceptionHandler
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
