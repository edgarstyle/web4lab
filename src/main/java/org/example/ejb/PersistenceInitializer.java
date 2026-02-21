package org.example.ejb;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.sql.SQLException;

@Singleton
@Startup
public class PersistenceInitializer {
    
    @PersistenceUnit(unitName = "web4PU")
    private EntityManagerFactory emf;
    
    @PostConstruct
    public void initialize() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            // Пытаемся создать таблицу users
            createTableIfNotExists(em, "users",
                "CREATE TABLE users (" +
                "ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "passwordHash VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (ID)" +
                ")"
            );
            
            // Пытаемся создать таблицу results
            createTableIfNotExists(em, "results",
                "CREATE TABLE results (" +
                "ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, " +
                "X DECIMAL(31,15) NOT NULL, " +
                "Y DECIMAL(31,15) NOT NULL, " +
                "R DECIMAL(31,15) NOT NULL, " +
                "HIT BOOLEAN NOT NULL, " +
                "TIMESTAMP TIMESTAMP NOT NULL, " +
                "EXECUTIONTIME BIGINT, " +
                "user_id BIGINT NOT NULL, " +
                "PRIMARY KEY (ID), " +
                "FOREIGN KEY (user_id) REFERENCES users(ID)" +
                ")"
            );
            
            em.getTransaction().commit();
            System.out.println("База данных успешно инициализирована");
            
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                try {
                    em.getTransaction().rollback();
                } catch (Exception ex) {
                    // Игнорируем
                }
            }
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    private void createTableIfNotExists(EntityManager em, String tableName, String createSql) {
        try {
            System.out.println("Создание таблицы " + tableName + "...");
            em.createNativeQuery(createSql).executeUpdate();
            System.out.println("Таблица " + tableName + " создана");
        } catch (Exception e) {
            // Проверяем, является ли ошибка следствием того, что таблица уже существует
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLException) {
                    SQLException sqlEx = (SQLException) cause;
                    String errorCode = sqlEx.getSQLState();
                    String message = sqlEx.getMessage();
                    
                    // В Derby код ошибки для "table already exists" обычно X0Y32 или 42X95
                    if (errorCode != null && (errorCode.equals("X0Y32") || errorCode.equals("42X95") || 
                        (message != null && message.toUpperCase().contains("ALREADY EXISTS")))) {
                        System.out.println("Таблица " + tableName + " уже существует");
                        return;
                    }
                }
                cause = cause.getCause();
            }
            
            // Если это не ошибка "table already exists", пробрасываем исключение дальше
            throw new RuntimeException("Ошибка при создании таблицы " + tableName + ": " + e.getMessage(), e);
        }
    }
}

