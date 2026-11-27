# WorkSpace Lite

## Objectif
Gérer les salles, les équipements et les réservations d’une entreprise.

## Utilisateurs
- Administrateur : gère les salles, les équipements, les employés et les départements.
- Employé : consulte les salles et réserve un créneau.

## Fonctionnalités
- Inscription publique et connexion des employés ; l’inscription attribue le rôle `EMPLOYE`.
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

Prérequis : Java 21 et Node.js avec npm.

Compte administrateur initial : identifiant `admin`, mot de passe `admin`.

Le CSS généré par Tailwind est ignoré par Git. Après un nouveau clone, installez les dépendances et compilez le CSS avant de lancer l’application ou de construire le JAR. Relancez `npm run build:css` après une modification des sources Tailwind ou des classes Tailwind dans les templates.

Sous Linux/macOS :

```bash
npm ci
npm run build:css
./mvnw spring-boot:run
```

Sous Windows :

```powershell
npm ci
npm run build:css
.\mvnw.cmd spring-boot:run
```
