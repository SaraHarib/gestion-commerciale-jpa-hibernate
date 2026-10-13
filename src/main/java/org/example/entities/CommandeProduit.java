package org.example.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "commande_produit")
public class CommandeProduit {
    // Conteneur de l'identifiant dérivé. @MapsId le remplit lors de persist/flush,
    // après génération de l'ID de la commande : aucun ID n'est copié du parent transitoire.
    @EmbeddedId
    private CommandeProduitPK id = new CommandeProduitPK();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    public CommandeProduit() {
    }

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = Objects.requireNonNull(commande, "commande");
        this.produit = Objects.requireNonNull(produit, "produit");
        setQuantite(quantite);
    }

    public CommandeProduitPK getId() { return id; }
    public void setId(CommandeProduitPK id) { this.id = Objects.requireNonNull(id, "id"); }
    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }

    public void setQuantite(int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive.");
        }
        this.quantite = quantite;
    }
}
