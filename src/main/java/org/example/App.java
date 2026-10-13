package org.example;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.example.dao.GenericDAO;
import org.example.dto.CategorySalesReport;
import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.entities.User;
import org.example.service.CrudService;
import org.example.service.DemoDataService;
import org.example.service.ProduitService;
import org.example.service.SalesReportService;
import org.example.util.JpaUtil;
import org.example.util.Transactions;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        JpaUtil.getEntityManagerFactory();
        try {
            demonstrate();
        } finally {
            JpaUtil.shutdown();
        }
    }

    private static void demonstrate() {
        System.out.println("========================================");
        System.out.println("TP6 - GESTION COMMERCIALE JPA / HIBERNATE");
        System.out.println("========================================");
        DemoDataService data = new DemoDataService();
        GenericDAO<Produit, Long> produitDao = new GenericDAO<>(Produit.class);
        CrudService<Produit, Long> crud = new CrudService<>(produitDao);
        ProduitService produits = new ProduitService();

        section("1. CREATION DES CATEGORIES ET PRODUITS");
        System.out.println("Les données [TP6] existantes sont réutilisées à chaque lancement. Prix en MAD.");
        Categorie informatique = data.categorie("Informatique [TP6]");
        Categorie bureautique = data.categorie("Bureautique [TP6]");
        Categorie mobilier = data.categorie("Mobilier [TP6]");
        List<Categorie> categories = Arrays.asList(informatique, bureautique, mobilier);
        categories.forEach(System.out::println);
        Produit portable = data.produit("Ordinateur portable", new BigDecimal("8500.00"), informatique);
        Produit clavier = data.produit("Clavier mécanique", new BigDecimal("650.00"), informatique);
        Produit souris = data.produit("Souris sans fil", new BigDecimal("180.00"), informatique);
        Produit imprimante = data.produit("Imprimante laser", new BigDecimal("2400.00"), bureautique);
        Produit papier = data.produit("Ramette papier", new BigDecimal("55.00"), bureautique);
        Produit chaise = data.produit("Chaise ergonomique", new BigDecimal("1750.00"), mobilier);
        List<Produit> catalogue = Arrays.asList(portable, clavier, souris, imprimante, papier, chaise);
        catalogue.forEach(System.out::println);
        System.out.println("GenericDAO.find : " + produitDao.find(portable.getId()));
        System.out.println("CrudService.getById : " + crud.getById(clavier.getId()));
        System.out.println("CrudService.getAll : " + crud.getAll().size() + " produits");
        boolean crudVerified = demonstrateCrud(crud, informatique);

        section("2. RELATION CATEGORIE -> PRODUITS");
        Categorie relation = loadCategorieWithProduits(informatique.getId());
        System.out.println(relation);
        relation.getProduits().forEach(System.out::println);

        section("3. NAMED QUERIES");
        List<Produit> allNamed = produits.findAllNamed();
        List<Produit> byCategorie = produits.findByCategorie(informatique.getId());
        List<Produit> byLibelle = produits.searchByLibelle("CLAVIER");
        printProduits("Produit.findAll", allNamed);
        printProduits("Produit.findByCategorie : Informatique [TP6]", byCategorie);
        printProduits("Produit.searchByLibelle : CLAVIER", byLibelle);

        section("4. RECHERCHE CRITERIA");
        BigDecimal minPrix = new BigDecimal("100.00");
        BigDecimal maxPrix = new BigDecimal("1000.00");
        List<Produit> criteria = produits.search("CLAVIER", minPrix, maxPrix);
        printProduits("term=CLAVIER, minPrix=100.00, maxPrix=1000.00", criteria);

        section("5. PAGINATION");
        List<Produit> page1 = produits.findPage(1, 2);
        List<Produit> page2 = produits.findPage(2, 2);
        long total = produits.count();
        printProduits("Page 1 (pageSize = 2)", page1);
        printProduits("Page 2 (pageSize = 2)", page2);
        System.out.println("Total produits : " + total);

        section("6. HERITAGE JOINED");
        Client client = data.client();
        Fournisseur fournisseur = data.fournisseur();
        List<User> users = loadUsers();
        System.out.println("Requête polymorphique : SELECT u FROM User u ORDER BY u.id");
        for (User user : users) {
            System.out.println("Type réel : " + user.getClass().getSimpleName() + " - " + user);
            if (user instanceof Client) {
                System.out.println("  Date d'inscription : " + ((Client) user).getDateInscription());
            } else if (user instanceof Fournisseur) {
                System.out.println("  Adresse : " + ((Fournisseur) user).getAdresse());
            }
        }

        section("7. COMMANDE ET CLE COMPOSEE");
        YearMonth month = YearMonth.now();
        Commande commande = data.commande(month.atDay(1), portable, 2, papier, 5);
        System.out.println("ID commande : " + commande.getId() + " - Date : " + commande.getDateCommande());
        for (CommandeProduit ligne : commande.getLignes()) {
            System.out.println("Produit : " + ligne.getProduit().getLibelle() + " | quantité : "
                    + ligne.getQuantite() + " | clé composée : " + ligne.getId());
        }
        System.out.println("@MapsId dérive la clé de chaque ligne après génération de l'ID de commande.");

        section("8. TRANSACTION ET CONTEXTE DE PERSISTANCE");
        boolean transactionVerified = demonstrateTransaction(informatique.getId(), crud);

        section("9. RAPPORT DES VENTES");
        List<CategorySalesReport> report = new SalesReportService().getMonthlySalesReport(month);
        System.out.println("Mois : " + month + " - SUM(prix produit * quantité), par catégorie");
        report.forEach(System.out::println);

        section("10. VERIFICATION FINALE");
        CrudService<Categorie, Long> categorieCrud = new CrudService<>(new GenericDAO<>(Categorie.class));
        for (Categorie categorie : categories) {
            check(categorie.getId() != null && categorieCrud.getById(categorie.getId()) != null,
                    "La catégorie n'a pas été persistée : " + categorie.getNom());
        }
        for (Produit produit : catalogue) {
            check(produit.getId() != null && crud.getById(produit.getId()) != null,
                    "Le produit n'a pas été persisté : " + produit.getLibelle());
        }
        check(crudVerified, "Le cycle GenericDAO/CrudService create/find/findAll/update/delete a échoué.");
        check(ids(relation.getProduits()).containsAll(Arrays.asList(portable.getId(), clavier.getId(), souris.getId())),
                "La collection Categorie.produits ne contient pas les produits attendus.");
        check(relation.getProduits().stream().allMatch(p -> informatique.getId().equals(p.getCategorie().getId())),
                "La relation bidirectionnelle catégorie-produit est incorrecte.");
        check(new HashSet<>(ids(byCategorie)).equals(new HashSet<>(ids(relation.getProduits()))),
                "Produit.findByCategorie retourne des produits incorrects.");
        check(ids(allNamed).containsAll(ids(catalogue)) && allNamed.size() == total && isSorted(allNamed),
                "Produit.findAll ne retourne pas tous les produits triés par ID.");
        check(ids(byLibelle).contains(clavier.getId()) && byLibelle.stream().allMatch(
                        p -> p.getLibelle().toLowerCase(Locale.ROOT).contains("clavier")),
                "Produit.searchByLibelle ne respecte pas la recherche insensible à la casse.");
        check(ids(criteria).contains(clavier.getId()) && criteria.stream().allMatch(p ->
                        p.getLibelle().toLowerCase(Locale.ROOT).contains("clavier")
                                && p.getPrix().compareTo(minPrix) >= 0 && p.getPrix().compareTo(maxPrix) <= 0),
                "La recherche Criteria ne respecte pas les trois filtres.");
        check(produits.search(null, null, null).size() == total,
                "La recherche Criteria sans filtre doit retourner tous les produits.");
        check(page1.size() == 2 && page2.size() == 2
                        && ids(page1).equals(ids(allNamed.subList(0, 2)))
                        && ids(page2).equals(ids(allNamed.subList(2, 4))),
                "La pagination ne respecte pas l'ordre, la taille ou le décalage.");
        check(ids(produits.findPage(0, 0)).equals(ids(allNamed.subList(0, 1))),
                "La normalisation des paramètres de pagination est incorrecte.");
        check(users.stream().anyMatch(u -> u instanceof Client && u.getId().equals(client.getId()))
                        && users.stream().anyMatch(u -> u instanceof Fournisseur && u.getId().equals(fournisseur.getId())),
                "La requête polymorphique JOINED n'a pas restitué les sous-types attendus.");
        check(commande.getId() != null && commande.getLignes().size() == 2,
                "La commande doit avoir un ID et deux lignes persistées.");
        Map<Long, Integer> expectedQuantities = new HashMap<>();
        expectedQuantities.put(portable.getId(), 2);
        expectedQuantities.put(papier.getId(), 5);
        Set<Long> seenProducts = new HashSet<>();
        for (CommandeProduit ligne : commande.getLignes()) {
            Long produitId = ligne.getProduit().getId();
            check(Integer.valueOf(ligne.getQuantite()).equals(expectedQuantities.get(produitId))
                            && seenProducts.add(produitId),
                    "Produit ou quantité incorrects dans les lignes de commande.");
            check(ligne.getId() != null && commande.getId().equals(ligne.getId().getCommandeId())
                            && produitId.equals(ligne.getId().getProduitId())
                            && commande.getId().equals(ligne.getCommande().getId()),
                    "@MapsId n'a pas correctement affecté la clé composée de la ligne.");
        }
        check(transactionVerified, "La transaction ou la démonstration de l'état detached a échoué.");
        check(!report.isEmpty(), "Le rapport mensuel des ventes est vide.");
        verifyReport(report, informatique.getNom(), portable.getPrix().multiply(BigDecimal.valueOf(2)));
        verifyReport(report, bureautique.getNom(), papier.getPrix().multiply(BigDecimal.valueOf(5)));

        System.out.println("[OK] GenericDAO CRUD exécuté.");
        System.out.println("[OK] Relations JPA exécutées.");
        System.out.println("[OK] Named Queries exécutées.");
        System.out.println("[OK] Criteria API exécutée.");
        System.out.println("[OK] Pagination exécutée.");
        System.out.println("[OK] Héritage JOINED exécuté.");
        System.out.println("[OK] Clé composée exécutée.");
        System.out.println("[OK] Transactions exécutées.");
        System.out.println("[OK] Rapport de ventes exécuté.");
        System.out.println("[OK] Toutes les vérifications du TP6 ont réussi.");
    }

    private static boolean demonstrateCrud(CrudService<Produit, Long> crud, Categorie categorie) {
        Produit temporaire = crud.create(new Produit("Accessoire temporaire [TP6 CRUD]", new BigDecimal("25.00"), categorie));
        Long id = temporaire.getId();
        boolean verified;
        try {
            System.out.println("CrudService.create : " + temporaire);
            check(id != null && crud.getById(id) != null, "Le produit CRUD temporaire n'a pas été créé.");
            check(ids(crud.getAll()).contains(id), "GenericDAO.findAll ne contient pas le produit CRUD.");
            temporaire.setPrix(new BigDecimal("30.00"));
            Produit updated = crud.update(temporaire);
            System.out.println("CrudService.update : " + updated);
            verified = crud.getById(id).getPrix().compareTo(new BigDecimal("30.00")) == 0;
        } finally {
            crud.delete(temporaire);
        }
        System.out.println("CrudService.delete : produit temporaire supprimé, ID=" + id);
        return verified && crud.getById(id) == null;
    }

    private static boolean demonstrateTransaction(Long categorieId, CrudService<Produit, Long> crud) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = null;
        Produit produit = null;
        boolean managed;
        try {
            tx = em.getTransaction();
            tx.begin();
            produit = new Produit("Accessoire temporaire [TP6 transaction]", new BigDecimal("75.00"),
                    em.getReference(Categorie.class, categorieId));
            em.persist(produit);
            managed = em.contains(produit);
            System.out.println("EntityManager.contains(produit) pendant le contexte : " + managed);
            tx.commit();
            System.out.println("Transaction commit réussie - ID produit : " + produit.getId());
        } catch (RuntimeException | Error ex) {
            Transactions.rollback(tx, ex);
            throw ex;
        } finally {
            em.close();
        }

        try {
            System.out.println("EntityManager fermé : " + !em.isOpen());
            System.out.println("État detached : le produit conserve son ID, mais son contexte est fermé.");
            System.out.println("contains() n'est pas appelé sur le contexte fermé ; aucune relation LAZY n'est parcourue.");
            EntityManager autreContexte = JpaUtil.createEntityManager();
            try {
                boolean detached = !autreContexte.contains(produit);
                System.out.println("Nouveau contexte, contains(ancienne instance) : " + !detached);
                Produit reloaded = autreContexte.find(Produit.class, produit.getId());
                System.out.println("Nouveau contexte, contains(instance relue) : " + autreContexte.contains(reloaded));
                return managed && detached && reloaded != null && reloaded != produit
                        && autreContexte.contains(reloaded) && !em.isOpen();
            } finally {
                autreContexte.close();
            }
        } finally {
            crud.delete(produit);
            System.out.println("Produit transactionnel temporaire supprimé après vérification.");
        }
    }

    private static Categorie loadCategorieWithProduits(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT c FROM Categorie c LEFT JOIN FETCH c.produits WHERE c.id = :id", Categorie.class)
                    .setParameter("id", id).getSingleResult();
        } finally {
            em.close();
        }
    }

    private static List<User> loadUsers() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    private static List<Long> ids(List<Produit> produits) {
        return produits.stream().map(Produit::getId).collect(Collectors.toList());
    }

    private static boolean isSorted(List<Produit> produits) {
        for (int i = 1; i < produits.size(); i++) {
            if (produits.get(i - 1).getId() >= produits.get(i).getId()) {
                return false;
            }
        }
        return true;
    }

    private static void verifyReport(List<CategorySalesReport> report, String categorie, BigDecimal expectedMinimum) {
        check(report.stream().anyMatch(row -> categorie.equals(row.getCategorie())
                        && row.getChiffreAffaires().compareTo(expectedMinimum) >= 0),
                "Le rapport n'inclut pas le chiffre d'affaires attendu pour " + categorie + ".");
    }

    private static void printProduits(String label, List<Produit> produits) {
        System.out.println(label + " :");
        produits.forEach(System.out::println);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println(title);
        System.out.println("----------------------------------------");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
