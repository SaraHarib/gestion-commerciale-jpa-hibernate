package org.example;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.example.entities.Client;
import org.example.entities.CommandeProduit;
import org.example.entities.CommandeProduitPK;
import org.example.entities.Fournisseur;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Vérifie le démarrage ORM et les Named Queries sans connexion ni modification de MySQL. */
public class PersistenceMappingTest {
    private Map<String, Object> offlineProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.connection.provider_class",
                "org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.query.startup_check", "true");
        return properties;
    }

    @Test
    public void jpaUnitBootstrapsAllEntitiesAndValidatesNamedQueriesOffline() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU", offlineProperties());
        try {
            assertEquals(7, emf.getMetamodel().getEntities().size());
            assertEquals(CommandeProduitPK.class, emf.getMetamodel().entity(CommandeProduit.class).getIdType().getJavaType());
            SessionFactoryImplementor factory = emf.unwrap(SessionFactoryImplementor.class);
            assertTrue(factory.getMetamodel().entityPersister(Client.class) instanceof JoinedSubclassEntityPersister);
            assertTrue(factory.getMetamodel().entityPersister(Fournisseur.class) instanceof JoinedSubclassEntityPersister);
        } finally {
            emf.close();
        }
    }

    @Test
    public void nativeHibernateConfigurationBootstrapsSameEntitiesOffline() {
        Configuration configuration = new Configuration().configure();
        offlineProperties().forEach((name, value) -> configuration.setProperty(name, value.toString()));
        try (SessionFactory factory = configuration.buildSessionFactory()) {
            assertEquals(7, factory.getMetamodel().getEntities().size());
            assertEquals(CommandeProduitPK.class, factory.getMetamodel().entity(CommandeProduit.class).getIdType().getJavaType());
        }
    }
}
