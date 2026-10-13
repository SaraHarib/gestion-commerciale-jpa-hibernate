package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    // App utilise uniquement JpaUtil : cette fabrique native n'est donc pas créée par App.
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration().configure().buildSessionFactory();
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Impossible d'initialiser Hibernate via hibernate.cfg.xml.", ex);
            }
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
