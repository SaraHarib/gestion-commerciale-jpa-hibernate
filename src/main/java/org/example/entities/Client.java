package org.example.entities;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client extends User {
    private LocalDate dateInscription;

    public Client() {
    }

    public Client(String nom, LocalDate dateInscription) {
        super(nom);
        this.dateInscription = dateInscription;
    }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
}
