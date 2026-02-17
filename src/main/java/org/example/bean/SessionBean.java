package org.example.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.entity.Result;
import org.example.service.ResultService;
import org.example.util.ErrorHandler;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(SessionBean.class.getName());

    @Inject
    private ResultService resultService;

    private List<Result> results = new java.util.ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            if (resultService != null) {
                loadResults();
            } else {
                logger.warning("ResultService не инициализирован при создании SessionBean");
                results = new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            if (results == null) {
                results = new java.util.ArrayList<>();
            }
            logger.log(Level.WARNING, "Ошибка при инициализации SessionBean (БД может быть еще не готова)", e);
        }
    }

    public void loadResults() {
        try {
            if (resultService != null) {
                List<Result> loaded = resultService.findAll();
                if (loaded != null) {
                    results = loaded;
                    logger.fine("Загружено результатов: " + results.size());
                } else {
                    results = new java.util.ArrayList<>();
                }
            } else {
                logger.warning("Попытка загрузить результаты при неинициализированном ResultService");
                results = new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            if (results == null) {
                results = new java.util.ArrayList<>();
            }
            logger.log(Level.SEVERE, "Ошибка при загрузке результатов", e);
        }
    }

    public List<Result> getResults() {
        if (results == null) {
            results = new java.util.ArrayList<>();
        }
        return results;
    }

    public void addResult(Result result) {
        if (result == null) {
            logger.warning("Попытка добавить null результат");
            ErrorHandler.handleValidationError("Результат не может быть пустым");
            return;
        }
        
        if (resultService == null) {
            logger.severe("ResultService не инициализирован");
            ErrorHandler.handleError(new IllegalStateException("Сервис базы данных недоступен"), 
                    "Ошибка инициализации приложения. Перезагрузите страницу.");
            return;
        }
        
        try {
            resultService.save(result);
            loadResults();
            logger.fine("Результат успешно добавлен и загружен: " + result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении результата", e);
            ErrorHandler.handleError(e, "Не удалось сохранить результат. Попробуйте еще раз.");
        }
    }

    public void clearResults() {
        if (resultService == null) {
            logger.severe("ResultService не инициализирован");
            ErrorHandler.handleError(new IllegalStateException("Сервис базы данных недоступен"), 
                    "Ошибка инициализации приложения. Перезагрузите страницу.");
            return;
        }
        
        try {
            resultService.clearAll();
            loadResults();
            logger.info("Результаты успешно очищены");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при очистке результатов", e);
            ErrorHandler.handleError(e, "Не удалось очистить результаты. Попробуйте еще раз.");
        }
    }
}

