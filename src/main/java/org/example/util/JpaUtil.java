package org.example.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JpaUtil {
    private JpaUtil() {
    }

    // Le holder évite toute connexion pendant la construction d'un DAO ou un test unitaire.
    private static final class Holder {
        private static final EntityManagerFactory EMF = buildEntityManagerFactory();
    }

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            return Persistence.createEntityManagerFactory("defaultPU");
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Impossible d'initialiser JPA (defaultPU). Vérifiez MySQL et la base gestion_commerciale.", ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.EMF;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (Holder.EMF.isOpen()) {
            Holder.EMF.close();
        }
    }
}
