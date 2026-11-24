package com.example.ms_finanzas.exception;

// Se transforma en un error 400 en GlobalExceptionHandler
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
