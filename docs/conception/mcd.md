# MCD — WorkSpace Lite


## Entités et propriétés

Les identifiants (`id...`) sont conceptuels : ils ne fixent pas encore leur
type ni leur implémentation en base.

```mermaid
erDiagram
    DEPARTEMENT {
        identifiant idDepartement PK
        texte nom
    }
    EMPLOYE {
        identifiant idEmploye PK
        texte nom
        texte prenom
        texte email
        texte telephone
        texte motDePasseHash
    }
    SALLE {
        identifiant idSalle PK
        texte nom
        entier capacite
        texte localisation
    }
    TYPE_EQUIPEMENT {
        identifiant idType PK
        texte nom
    }
    EQUIPEMENT {
        identifiant idEquipement PK
        texte nom
        texte reference
        texte etat
        texte mobilite
    }
    RESERVATION {
        identifiant idReservation PK
        texte objet
        dateHeure dateDebut
        dateHeure dateFin
        entier nombreParticipants
    }

    DEPARTEMENT o|--o{ EMPLOYE : rattache
    TYPE_EQUIPEMENT ||--o{ EQUIPEMENT : categorise
    SALLE o|--o{ EQUIPEMENT : accueille
    SALLE ||--o{ RESERVATION : concerne
    EMPLOYE ||--o{ RESERVATION : organise
    RESERVATION o{--o{ EQUIPEMENT : reserve
```

## Cardinalités

| Association | Cardinalités | Sens |
|---|---|---|
| Département — Employé | Département `0..N`, employé `0..1` | Un employé peut ne pas encore être rattaché à un département. |
| Type — Équipement | Type `0..N`, équipement `1..1` | Un équipement physique a un type ; un type peut n'avoir aucun équipement. |
| Salle — Équipement | Salle `0..N`, équipement `0..1` | Un équipement peut être sans salle ou affecté à une seule salle. Un équipement fixe doit être rattaché à sa salle ; un mobile peut être sans salle. |
| Salle — Réservation | Salle `0..N`, réservation `1..1` | Chaque réservation porte sur une seule salle. |
| Employé — Réservation | Employé `0..N`, réservation `1..1` | Chaque réservation a un employé organisateur. |
| Réservation — Équipement | `0..N` de chaque côté | Une réservation peut réserver zéro ou plusieurs équipements mobiles ; un équipement mobile peut figurer dans plusieurs réservations à des dates différentes. |

## Règles métier associées

- La date de fin d'une réservation doit être postérieure à sa date de début.
- Deux réservations d'une même salle ne peuvent pas se chevaucher.
- Le nombre de participants ne peut pas dépasser la capacité de la salle.
- Dans la référence, un équipement mis en maintenance conserve sa salle pour qu'on puisse le localiser, mais ne peut pas être affecté ou réaffecté tant qu'il est en maintenance. À la remise en service, il redevient `AFFECTE` s'il est encore lié à une salle, sinon `DISPONIBLE`.
- Deux réservations de la même salle peuvent se suivre sans intervalle ; elles ne se chevauchent que si `nouveauDebut < finExistante` et `nouvelleFin > debutExistant`.
- La référence impose un email d'employé unique et une référence d'équipement unique.

## Hypothèses à valider avant le MLD

Ce brouillon reprend le modèle de la référence et les décisions déjà données par
le groupe. Chaque employé est aussi son compte de connexion : son email sert
d'identifiant, et son mot de passe sera stocké sous forme hachée. Une réservation
vise une seule salle, a un organisateur, conserve le nombre de participants sans
lister les personnes, et peut réserver du matériel facultativement. La
réservation de matériel est une extension au modèle de la référence, qui ne
l'implémente pas encore.

`mobilite` prend les valeurs `FIXE` ou `MOBILE`. Un équipement fixe est installé
dans sa salle et ne se réserve pas séparément. Un équipement mobile peut être
associé à une réservation ; il peut aussi être sans salle. La capacité de la
salle indique déjà le nombre de places ; il n'est donc pas nécessaire
d'associer à la salle des catégories génériques de matériel.

Un équipement mobile ne peut pas figurer dans deux réservations dont les
horaires se chevauchent.

## Hypothèses reprises de la référence

Le rattachement d'un employé à un département est facultatif dans la référence.
Les réservations en conflit sont refusées pour une même salle ; le code de
référence ne vérifie pas les chevauchements dans l'agenda d'un employé.
