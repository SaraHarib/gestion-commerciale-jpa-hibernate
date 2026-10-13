package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.util.JpaUtil;
import org.example.util.Transactions;

/** Données idempotentes : aucune suppression globale et aucun changement de schéma. */
public class DemoDataService {
    public Categorie categorie(String nom) {
        return existingOrCreate(em -> em.createQuery("SELECT c FROM Categorie c WHERE c.nom = :nom", Categorie.class)
                        .setParameter("nom", nom).getResultList(),
                () -> new CrudService<>(new GenericDAO<Categorie, Long>(Categorie.class)).create(new Categorie(nom)));
    }

    public Produit produit(String libelle, BigDecimal prix, Categorie categorie) {
        return existingOrCreate(em -> em.createQuery(
                        "SELECT p FROM Produit p WHERE p.categorie.id = :catId AND p.libelle = :libelle ORDER BY p.id", Produit.class)
                        .setParameter("catId", categorie.getId()).setParameter("libelle", libelle).getResultList(),
                () -> new CrudService<>(new GenericDAO<Produit, Long>(Produit.class))
                        .create(new Produit(libelle, prix, categorie)));
    }

    public Client client() {
        LocalDate inscription = LocalDate.of(2026, 1, 15);
        return existingOrCreate(em -> em.createQuery(
                        "SELECT c FROM Client c WHERE c.nom = :nom AND c.dateInscription = :date ORDER BY c.id", Client.class)
                        .setParameter("nom", "Sara El Mansouri").setParameter("date", inscription).getResultList(),
                () -> new CrudService<>(new GenericDAO<Client, Long>(Client.class))
                        .create(new Client("Sara El Mansouri", inscription)));
    }

    public Fournisseur fournisseur() {
        return existingOrCreate(em -> em.createQuery(
                        "SELECT f FROM Fournisseur f WHERE f.nom = :nom AND f.adresse = :adresse ORDER BY f.id", Fournisseur.class)
                        .setParameter("nom", "Atlas Distribution").setParameter("adresse", "Marrakech").getResultList(),
                () -> new CrudService<>(new GenericDAO<Fournisseur, Long>(Fournisseur.class))
                        .create(new Fournisseur("Atlas Distribution", "Marrakech")));
    }

    public Commande commande(LocalDate date, Produit premier, int quantitePremier, Produit second, int quantiteSecond) {
        Long id = Transactions.execute(em -> {
            List<Commande> candidates = em.createQuery(
                    "SELECT DISTINCT c FROM Commande c JOIN FETCH c.lignes l JOIN FETCH l.produit "
                            + "WHERE c.dateCommande = :date AND EXISTS "
                            + "(SELECT cp FROM CommandeProduit cp WHERE cp.commande = c AND cp.produit.id = :produitId)",
                    Commande.class).setParameter("date", date).setParameter("produitId", premier.getId()).getResultList();
            for (Commande candidate : candidates) {
                if (candidate.getLignes().size() == 2
                        && hasLigne(candidate, premier.getId(), quantitePremier)
                        && hasLigne(candidate, second.getId(), quantiteSecond)) {
                    return candidate.getId();
                }
            }
            Commande nouvelle = new Commande(date);
            // La commande est transitoire à cet instant ; @MapsId dérivera les clés au persist.
            nouvelle.addProduit(em.getReference(Produit.class, premier.getId()), quantitePremier);
            nouvelle.addProduit(em.getReference(Produit.class, second.getId()), quantiteSecond);
            em.persist(nouvelle);
            em.flush();
            return nouvelle.getId();
        });
        EntityManager em = JpaUtil.createEntityManager();
        try {
            // Les relations affichées dans App sont chargées avant la fermeture du contexte.
            return em.createQuery("SELECT DISTINCT c FROM Commande c JOIN FETCH c.lignes l "
                            + "JOIN FETCH l.produit WHERE c.id = :id", Commande.class)
                    .setParameter("id", id).getSingleResult();
        } finally {
            em.close();
        }
    }

    private boolean hasLigne(Commande commande, Long produitId, int quantite) {
        for (CommandeProduit ligne : commande.getLignes()) {
            if (produitId.equals(ligne.getProduit().getId()) && ligne.getQuantite() == quantite) {
                return true;
            }
        }
        return false;
    }

    private <T> T existingOrCreate(Function<EntityManager, List<T>> lookup, Supplier<T> create) {
        List<T> existing;
        EntityManager em = JpaUtil.createEntityManager();
        try {
            existing = lookup.apply(em);
        } finally {
            em.close();
        }
        return existing.isEmpty() ? create.get() : existing.get(0);
    }
}
