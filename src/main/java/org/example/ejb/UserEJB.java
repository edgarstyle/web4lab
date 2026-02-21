package org.example.ejb;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import org.example.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Stateless
public class UserEJB {
    
    @PersistenceUnit(unitName = "web4PU")
    private EntityManagerFactory emf;
    
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public User findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<User> query = em.createNamedQuery("User.findByUsername", User.class);
            query.setParameter("username", username);
            List<User> results = query.getResultList();
            em.getTransaction().commit();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public User createUser(String username, String password) {
        if (findByUsername(username) != null) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            User user = new User(username, passwordHash);
            em.persist(user);
            em.flush();
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean validatePassword(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        return BCrypt.checkpw(password, user.getPasswordHash());
    }

    public User authenticate(String username, String password) {
        if (validatePassword(username, password)) {
            return findByUsername(username);
        }
        return null;
    }

    public User findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, id);
            em.getTransaction().commit();
            return user;
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

