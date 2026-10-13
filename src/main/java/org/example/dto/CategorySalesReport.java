package org.example.dto;

import java.math.BigDecimal;

public class CategorySalesReport {
    private final String categorie;
    private final BigDecimal chiffreAffaires;

    public CategorySalesReport(String categorie, BigDecimal chiffreAffaires) {
        this.categorie = categorie;
        this.chiffreAffaires = chiffreAffaires;
    }

    public String getCategorie() { return categorie; }
    public BigDecimal getChiffreAffaires() { return chiffreAffaires; }

    @Override
    public String toString() {
        return categorie + " : " + chiffreAffaires + " MAD";
    }
}
