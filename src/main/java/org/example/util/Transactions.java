package org.example.util;

import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public final class Transactions {
    private Transactions() {
    }

    public static <T> T execute(Function<EntityManager, T> action) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            T result = action.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException | Error ex) {
            rollback(tx, ex);
            throw ex;
        } finally {
            em.close();
        }
    }

    public static void rollback(EntityTransaction tx, Throwable original) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (RuntimeException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }
}
