package org.example.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ProduitTest {
    @Test
    public void createsProductWithCategoryPriceAndCurrentDate() {
        LocalDate before = LocalDate.now();
        Categorie categorie = new Categorie("Informatique");
        Produit produit = new Produit("Ordinateur portable", new BigDecimal("8500.00"), categorie);
        assertEquals("Ordinateur portable", produit.getLibelle());
        assertEquals(new BigDecimal("8500.00"), produit.getPrix());
        assertSame(categorie, produit.getCategorie());
        assertFalse(produit.getDateCreation().isBefore(before));
        assertFalse(produit.getDateCreation().isAfter(LocalDate.now()));
    }

    @Test
    public void toStringDoesNotAccessCategory() {
        Categorie categorie = new Categorie("Informatique") {
            @Override
            public String toString() {
                throw new AssertionError("La relation ne doit pas être parcourue.");
            }
        };
        Produit produit = new Produit("Clavier", BigDecimal.TEN, categorie);
        categorie.addProduit(produit);
        assertTrue(produit.toString().contains("Clavier"));
    }
}
