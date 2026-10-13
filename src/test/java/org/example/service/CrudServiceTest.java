package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.junit.Test;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CrudServiceTest {
    // Construire le DAO ne doit pas ouvrir de connexion : ces tests fonctionnent sans MySQL.
    private final CrudService<Categorie, Long> service = new CrudService<>(new GenericDAO<>(Categorie.class));

    @Test
    public void createRejectsNullBeforeAccessingDatabase() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    public void updateRejectsNullBeforeAccessingDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.update(null));
    }

    @Test
    public void deleteRejectsNullBeforeAccessingDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(null));
    }
}
