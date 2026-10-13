package org.example.entities;

import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CategorieTest {
    @Test
    public void createsCategorieWithEmptyProducts() {
        Categorie categorie = new Categorie("Informatique");
        assertNull(categorie.getId());
        assertEquals("Informatique", categorie.getNom());
        assertTrue(categorie.getProduits().isEmpty());
    }

    @Test
    public void maintainsBothSidesWhenAddingAndRemoving() {
        Categorie categorie = new Categorie("Bureautique");
        Produit produit = new Produit("Papier", new BigDecimal("55.00"), null);
        categorie.addProduit(produit);
        categorie.addProduit(produit);
        assertEquals(1, categorie.getProduits().size());
        assertSame(categorie, produit.getCategorie());
        categorie.removeProduit(produit);
        assertTrue(categorie.getProduits().isEmpty());
        assertNull(produit.getCategorie());
    }

    @Test
    public void movingTransientProductUpdatesBothCollections() {
        Categorie initial = new Categorie("Initiale");
        Categorie destination = new Categorie("Destination");
        Produit produit = new Produit("Clavier", BigDecimal.TEN, initial);
        initial.addProduit(produit);
        destination.addProduit(produit);
        assertFalse(initial.getProduits().contains(produit));
        assertTrue(destination.getProduits().contains(produit));
        assertSame(destination, produit.getCategorie());
    }

    @Test
    public void replacingCollectionUnlinksRemovedProducts() {
        Categorie categorie = new Categorie("Mobilier");
        Produit ancien = new Produit("Ancien", BigDecimal.ONE, categorie);
        Produit nouveau = new Produit("Nouveau", BigDecimal.TEN, categorie);
        categorie.addProduit(ancien);
        categorie.setProduits(Arrays.asList(nouveau));
        assertNull(ancien.getCategorie());
        assertSame(categorie, nouveau.getCategorie());
        assertEquals(Arrays.asList(nouveau), categorie.getProduits());
    }
}
