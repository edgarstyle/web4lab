package org.example.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.example.entity.Result;
import org.example.exception.DatabaseException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ResultService {
    private static final Logger logger = Logger.getLogger(ResultService.class.getName());

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            emf = Persistence.createEntityManagerFactory("web3PU");
            if (emf != null) {
                entityManager = emf.createEntityManager();
                logger.info("EntityManager успешно инициализирован");
            } else {
                logger.severe("Не удалось создать EntityManagerFactory");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Критическая ошибка при инициализации EntityManagerFactory", e);
            throw new DatabaseException("Не удалось инициализировать подключение к базе данных", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public void save(Result result) {
        if (result == null) {
            throw new IllegalArgumentException("Результат не может быть null");
        }
        
        if (entityManager == null || !entityManager.isOpen()) {
            logger.severe("Попытка сохранить результат при неинициализированном EntityManager");
            throw new DatabaseException("Сервис базы данных недоступен. Попробуйте позже.");
        }
        
        if (result.getTimestamp() == null) {
            result.setTimestamp(java.time.LocalDateTime.now());
        }
        
        EntityTransaction tx = entityManager.getTransaction();
        boolean transactionStarted = false;
        
        try {
            if (!tx.isActive()) {
                tx.begin();
                transactionStarted = true;
            }
            
            entityManager.persist(result);
            entityManager.flush();
            
            if (transactionStarted && tx.isActive()) {
                tx.commit();
            }
            
            logger.fine("Результат успешно сохранен: " + result);
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                    logger.warning("Транзакция откачена из-за ошибки сохранения");
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Ошибка при сохранении результата", e);
            throw new DatabaseException("Не удалось сохранить результат в базу данных", e);
        } catch (Exception e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Неожиданная ошибка при сохранении результата", e);
            throw new DatabaseException("Произошла ошибка при сохранении результата", e);
        }
    }

    public List<Result> findAll() {
        if (entityManager == null || !entityManager.isOpen()) {
            logger.warning("Попытка получить результаты при неинициализированном EntityManager");
            return new java.util.ArrayList<>();
        }
        
        EntityTransaction tx = entityManager.getTransaction();
        boolean transactionStarted = false;
        
        try {
            if (!tx.isActive()) {
                tx.begin();
                transactionStarted = true;
            }
            
            TypedQuery<Result> query = entityManager.createNamedQuery("Result.findAll", Result.class);
            List<Result> results = query.getResultList();
            
            if (transactionStarted && tx.isActive()) {
                tx.commit();
            }
            
            return results != null ? results : new java.util.ArrayList<>();
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Ошибка при получении результатов из базы данных", e);
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Неожиданная ошибка при получении результатов", e);
            return new java.util.ArrayList<>();
        }
    }

    public void clearAll() {
        if (entityManager == null || !entityManager.isOpen()) {
            logger.warning("Попытка очистить результаты при неинициализированном EntityManager");
            throw new DatabaseException("Сервис базы данных недоступен. Попробуйте позже.");
        }
        
        EntityTransaction tx = entityManager.getTransaction();
        boolean transactionStarted = false;
        
        try {
            if (!tx.isActive()) {
                tx.begin();
                transactionStarted = true;
            }
            
            int deletedCount = entityManager.createQuery("DELETE FROM Result").executeUpdate();
            entityManager.flush();
            
            if (transactionStarted && tx.isActive()) {
                tx.commit();
            }
            
            logger.info("Очищено результатов: " + deletedCount);
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Ошибка при очистке результатов", e);
            throw new DatabaseException("Не удалось очистить результаты из базы данных", e);
        } catch (Exception e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    logger.log(Level.SEVERE, "Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Неожиданная ошибка при очистке результатов", e);
            throw new DatabaseException("Произошла ошибка при очистке результатов", e);
        }
    }
}

