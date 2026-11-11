# WorkSpace Lite

## Objectif
Gérer les salles, les équipements et les réservations d’une entreprise.

## Utilisateurs
- Administrateur : gère les salles, les équipements, les employés et les départements.
- Employé : consulte les salles et réserve un créneau.

## Fonctionnalités
- Gérer les départements et les employés.
- Gérer les salles et leurs équipements.
- Réserver une ou plusieurs salles.
- Empêcher les réservations qui se chevauchent.
- Afficher un tableau de bord.
- (Inviter les employés à la réunion ?)

## Règles métier
- Une réservation concerne une salle (plusieurs en cas de grosse réunion ?), un employé (liste d'employés ?) et un créneau.
- Une salle / un équipement ne peut pas avoir deux réservations qui se chevauchent.
- Le nombre de participants ne dépasse pas la capacité de la salle.


## Technologies envisagées
- Java 21
- Spring Boot
- Spring MVC et Thymeleaf
- Tailwind
- Spring Data JPA
- H2
- Maven

## Développement local

Prérequis : Java 21.

Sous Linux/macOS :

```bash
npm ci 
./mvnw spring-boot:run
```

Sous Windows :

mvnw.cmd spring-boot:run