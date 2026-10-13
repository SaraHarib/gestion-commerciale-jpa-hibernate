package org.example.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CommandeTest {
    @Test
    public void acceptsLinesBeforeOrderHasGeneratedId() {
        Commande commande = new Commande(LocalDate.now());
        Produit produit = new Produit("Clavier", BigDecimal.TEN, new Categorie("Informatique"));
        produit.setId(42L);
        commande.addProduit(produit, 2);
        CommandeProduit ligne = commande.getLignes().get(0);
        assertNull(commande.getId());
        assertNotNull(ligne.getId());
        assertNull(ligne.getId().getCommandeId());
        assertNull(ligne.getId().getProduitId());
        assertSame(commande, ligne.getCommande());
        assertSame(produit, ligne.getProduit());
        assertEquals(2, ligne.getQuantite());
    }

    @Test
    public void rejectsNonPositiveQuantitiesInConstructorAndSetter() {
        Commande commande = new Commande(LocalDate.now());
        Produit produit = new Produit("Papier", BigDecimal.ONE, null);
        assertThrows(IllegalArgumentException.class, () -> new CommandeProduit(commande, produit, 0));
        assertThrows(IllegalArgumentException.class, () -> new CommandeProduit(commande, produit, -1));
        CommandeProduit ligne = new CommandeProduit(commande, produit, 1);
        assertThrows(IllegalArgumentException.class, () -> ligne.setQuantite(0));
        assertEquals(1, ligne.getQuantite());
    }

    @Test
    public void sameProductMergesQuantitiesInsteadOfDuplicatingPrimaryKey() {
        Commande commande = new Commande(LocalDate.now());
        Produit produit = new Produit("Papier", BigDecimal.ONE, null);
        produit.setId(10L);
        Produit sameProduct = new Produit("Papier", BigDecimal.ONE, null);
        sameProduct.setId(10L);
        commande.addProduit(produit, 2);
        commande.addProduit(sameProduct, 3);
        assertEquals(1, commande.getLignes().size());
        assertEquals(5, commande.getLignes().get(0).getQuantite());
    }

    @Test
    public void removingLineUpdatesBothSides() {
        Commande commande = new Commande(LocalDate.now());
        commande.addProduit(new Produit("Papier", BigDecimal.ONE, null), 1);
        CommandeProduit ligne = commande.getLignes().get(0);
        commande.removeLigne(ligne);
        assertTrue(commande.getLignes().isEmpty());
        assertNull(ligne.getCommande());
    }
}
