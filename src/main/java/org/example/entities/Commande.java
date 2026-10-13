package org.example.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateCommande;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();

    public Commande() {
    }

    public Commande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }
    public List<CommandeProduit> getLignes() { return lignes; }

    public void setLignes(List<CommandeProduit> lignes) {
        List<CommandeProduit> replacement = new ArrayList<>(Objects.requireNonNull(lignes, "lignes"));
        for (CommandeProduit ligne : replacement) {
            Objects.requireNonNull(ligne, "ligne");
            if (ligne.getCommande() != null && ligne.getCommande() != this) {
                throw new IllegalArgumentException("Une ligne ne peut pas changer de commande.");
            }
        }
        for (CommandeProduit ligne : new ArrayList<>(this.lignes)) {
            if (!replacement.contains(ligne)) {
                removeLigne(ligne);
            }
        }
        for (CommandeProduit ligne : replacement) {
            if (!this.lignes.contains(ligne)) {
                this.lignes.add(ligne);
            }
            ligne.setCommande(this);
        }
    }

    public void addProduit(Produit produit, int quantite) {
        Objects.requireNonNull(produit, "produit");
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive.");
        }
        for (CommandeProduit ligne : lignes) {
            Produit existing = ligne.getProduit();
            if (existing == produit || (produit.getId() != null && produit.getId().equals(existing.getId()))) {
                ligne.setQuantite(Math.addExact(ligne.getQuantite(), quantite));
                return;
            }
        }
        lignes.add(new CommandeProduit(this, produit, quantite));
    }

    public void removeLigne(CommandeProduit ligne) {
        if (lignes.remove(ligne)) {
            ligne.setCommande(null);
        }
    }
}
