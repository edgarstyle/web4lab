package org.example.util;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.example.exception.ApplicationException;
import org.example.exception.DatabaseException;
import org.example.exception.ValidationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Утилита для централизованной обработки ошибок
 */
public class ErrorHandler {
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    /**
     * Обрабатывает исключение и добавляет сообщение пользователю
     */
    public static void handleError(Exception e, String defaultMessage) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            logger.log(Level.SEVERE, "FacesContext недоступен. Ошибка: " + defaultMessage, e);
            return;
        }

        String userMessage;
        Level logLevel;

        if (e instanceof ValidationException) {
            userMessage = e.getMessage();
            logLevel = Level.WARNING;
        } else if (e instanceof DatabaseException) {
            userMessage = e.getMessage();
            logLevel = Level.SEVERE;
        } else if (e instanceof ApplicationException) {
            userMessage = ((ApplicationException) e).getUserMessage();
            logLevel = Level.SEVERE;
        } else {
            userMessage = defaultMessage != null ? defaultMessage : "Произошла непредвиденная ошибка. Попробуйте позже.";
            logLevel = Level.SEVERE;
        }

        logger.log(logLevel, "Ошибка: " + userMessage, e);

        context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Ошибка",
                userMessage
        ));
    }

    /**
     * Обрабатывает ошибку валидации
     */
    public static void handleValidationError(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ошибка валидации",
                    message
            ));
        }
        logger.log(Level.WARNING, "Ошибка валидации: " + message);
    }

    /**
     * Обрабатывает ошибку базы данных
     */
    public static void handleDatabaseError(String message, Throwable cause) {
        handleError(new DatabaseException(message, cause), null);
    }
}

