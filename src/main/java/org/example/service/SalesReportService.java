package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import org.example.dto.CategorySalesReport;
import org.example.entities.Categorie;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import org.example.util.JpaUtil;

public class SalesReportService {
    public List<CategorySalesReport> getMonthlySalesReport(YearMonth month) {
        Objects.requireNonNull(month, "month");
        EntityManager em = JpaUtil.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<CategorySalesReport> query = cb.createQuery(CategorySalesReport.class);
            Root<CommandeProduit> ligne = query.from(CommandeProduit.class);
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");
            Join<CommandeProduit, Commande> commande = ligne.join("commande");
            Expression<BigDecimal> montant = cb.prod(produit.<BigDecimal>get("prix"),
                    ligne.<Integer>get("quantite").as(BigDecimal.class));
            query.select(cb.construct(CategorySalesReport.class, categorie.get("nom"), cb.sum(montant)))
                    .where(cb.greaterThanOrEqualTo(commande.<LocalDate>get("dateCommande"), month.atDay(1)),
                            cb.lessThan(commande.<LocalDate>get("dateCommande"), month.plusMonths(1).atDay(1)))
                    .groupBy(categorie.get("id"), categorie.get("nom"))
                    .orderBy(cb.asc(categorie.get("nom")));
            return em.createQuery(query).getResultList();
        } finally {
            em.close();
        }
    }
}
