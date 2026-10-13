# TP6 - Gestion commerciale avec JPA et Hibernate

Ce projet met en pratique Hibernate/JPA, le GenericDAO, les relations JPA, les Named Queries, Criteria API, la pagination, l’héritage JOINED, les clés composées et un rapport de ventes.

## Captures d’écran

### 1. GenericDAO et opérations CRUD

![GenericDAO CRUD](images/01-generic-dao-crud.png)

Exécution des principales opérations CRUD à travers le GenericDAO et le service générique.

---

### 2. Relation Catégorie - Produits

![Relation Catégorie Produits](images/02-relation-categorie-produits.png)

Vérification de la relation bidirectionnelle entre une catégorie et ses produits.

---

### 3. Named Queries

![Named Queries](images/03-named-queries.png)

Exécution des Named Queries pour récupérer les produits et effectuer une recherche par libellé.

---

### 4. Recherche avec Criteria API

![Criteria API](images/04-recherche-criteria.png)

Recherche dynamique de produits selon plusieurs critères comme le libellé et l’intervalle de prix.

---

### 5. Pagination

![Pagination](images/05-pagination.png)

Pagination des produits avec `setFirstResult` et `setMaxResults`.

---

### 6. Héritage JOINED

![Héritage JOINED](images/06-heritage-joined.png)

Requête polymorphique sur la hiérarchie `User`, `Client` et `Fournisseur` utilisant la stratégie JOINED.

---

### 7. Clé composée

![Clé composée](images/07-cle-composee.png)

Gestion de la relation entre commandes et produits avec une entité associative et une clé composée.

---

### 8. Rapport des ventes

![Rapport des ventes](images/08-rapport-ventes.png)

Calcul du chiffre d’affaires par catégorie à partir du prix des produits et des quantités commandées.

---

### 9. Validation finale

![Validation finale](images/09-validation-finale-build-success.png)

Validation finale du TP6 avec toutes les fonctionnalités exécutées avec succès et un `BUILD SUCCESS`.

## Résultat

Toutes les fonctionnalités principales du TP6 ont été exécutées et validées avec succès.
