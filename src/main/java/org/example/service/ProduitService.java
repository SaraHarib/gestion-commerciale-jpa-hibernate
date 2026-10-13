package org.example.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.example.entities.Produit;
import org.example.util.JpaUtil;

public class ProduitService {
    public List<Produit> findAllNamed() {
        return read(em -> em.createNamedQuery("Produit.findAll", Produit.class).getResultList());
    }

    public List<Produit> findByCategorie(Long catId) {
        return read(em -> em.createNamedQuery("Produit.findByCategorie", Produit.class)
                .setParameter("catId", catId).getResultList());
    }

    public List<Produit> searchByLibelle(String term) {
        return read(em -> em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                .setParameter("term", term == null ? "" : term).getResultList());
    }

    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        return read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> query = cb.createQuery(Produit.class);
            Root<Produit> produit = query.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();
            if (term != null && !term.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(produit.get("libelle")),
                        "%" + term.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.greaterThanOrEqualTo(produit.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.lessThanOrEqualTo(produit.get("prix"), maxPrix));
            }
            query.select(produit).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(produit.get("id")));
            return em.createQuery(query).getResultList();
        });
    }

    public List<Produit> findPage(int pageNumber, int pageSize) {
        int normalizedPage = Math.max(1, pageNumber);
        int normalizedSize = Math.max(1, pageSize);
        long offset = (long) (normalizedPage - 1) * normalizedSize;
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Le décalage de pagination dépasse la limite JPA.");
        }
        return read(em -> em.createQuery("SELECT p FROM Produit p ORDER BY p.id ASC", Produit.class)
                .setFirstResult((int) offset).setMaxResults(normalizedSize).getResultList());
    }

    public long count() {
        return read(em -> em.createQuery("SELECT COUNT(p) FROM Produit p", Long.class).getSingleResult());
    }

    private <R> R read(Function<EntityManager, R> action) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return action.apply(em);
        } finally {
            em.close();
        }
    }
}
