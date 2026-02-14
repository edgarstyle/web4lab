package org.example.exception;

/**
 * Базовое исключение приложения
 */
public class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String userMessage;

    public ApplicationException(String message) {
        super(message);
        this.userMessage = message;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
    }

    public ApplicationException(String userMessage, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}

