package org.example.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import org.example.util.JpaUtil;
import org.example.util.Transactions;

public class GenericDAO<T, ID extends Serializable> {
    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
    }

    public T find(ID id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return em.createQuery(query).getResultList();
        } finally {
            em.close();
        }
    }

    public T create(T entity) {
        return Transactions.execute(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public T update(T entity) {
        return Transactions.execute(em -> em.merge(entity));
    }

    public void delete(T entity) {
        Transactions.execute(em -> {
            T attached = em.contains(entity) ? entity : em.merge(entity);
            em.remove(attached);
            return null;
        });
    }
}
