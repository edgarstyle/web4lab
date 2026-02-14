package org.example.exception;

/**
 * Исключение для ошибок валидации
 */
public class ValidationException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

