package org.example.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.example.dao.GenericDAO;

public class CrudService<T, ID extends Serializable> {
    private final GenericDAO<T, ID> dao;

    public CrudService(GenericDAO<T, ID> dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    public T getById(ID id) { return dao.find(id); }
    public List<T> getAll() { return dao.findAll(); }

    public T create(T entity) {
        requireEntity(entity);
        return dao.create(entity);
    }

    public T update(T entity) {
        requireEntity(entity);
        return dao.update(entity);
    }

    public void delete(T entity) {
        requireEntity(entity);
        dao.delete(entity);
    }

    private void requireEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être null.");
        }
    }
}
