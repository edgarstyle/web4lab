package org.example.exception;

/**
 * Исключение для ошибок базы данных
 */
public class DatabaseException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super("Ошибка базы данных: " + message);
    }

    public DatabaseException(String message, Throwable cause) {
        super("Ошибка базы данных: " + message, cause);
    }
}

