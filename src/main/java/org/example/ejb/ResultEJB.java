package org.example.ejb;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import org.example.entity.Result;
import org.example.entity.User;
import org.example.util.AreaChecker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ResultEJB {
    
    @PersistenceUnit(unitName = "web4PU")
    private EntityManagerFactory emf;
    
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Result checkPoint(BigDecimal x, BigDecimal y, BigDecimal r, User user) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            
            long startTime = System.nanoTime();
            
            boolean hit = AreaChecker.checkHit(x, y, r);
            
            long executionTime = (System.nanoTime() - startTime) / 1000; // микросекунды
            
            Result result = new Result(x, y, r, hit);
            result.setTimestamp(LocalDateTime.now());
            result.setExecutionTime(executionTime);
            result.setUser(user);
            
            em.persist(result);
            em.flush();
            em.getTransaction().commit();
            
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Result> getResultsByUser(Long userId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Result> query = em.createNamedQuery("Result.findByUser", Result.class);
            query.setParameter("userId", userId);
            List<Result> results = query.getResultList();
            em.getTransaction().commit();
            return results;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void clearResultsByUser(Long userId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Result r WHERE r.user.id = :userId")
              .setParameter("userId", userId)
              .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}

