# 🚗 Covoiturage API

## 📌 Présentation

Covoiturage API est une application REST développée avec **Java et Spring Boot** permettant de gérer une plateforme de covoiturage.

Elle permet aux conducteurs de publier des trajets, aux passagers de rechercher des trajets disponibles et d'effectuer des réservations.

Le projet a été réalisé dans le cadre de la formation **Développement Web à YouCode**.

## 🛠️ Technologies utilisées

- **Java 17**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **Docker Compose**
- **Maven**
- **MapStruct**
- **Bean Validation**
- **JPQL**
- **JUnit 5 / Mockito**
- **SLF4J**

## 🏗️ Architecture du projet

Le projet suit une architecture en couches :

```text
src/main/java/com/covoiturage/covoiturage_api/
│
├── controller/       # Endpoints REST
├── service/          # Interfaces des services
│   └── impl/         # Logique métier
├── repository/       # Accès aux données
├── entity/           # Entités JPA
├── dto/              # Objets de transfert
├── mapper/           # Conversion Entity / DTO
└── exception/        # Gestion des exceptions
```

## ⚙️ Fonctionnalités principales

### Gestion des conducteurs
- Création et modification des profils
- Consultation des conducteurs
- Recherche par nom ou email

### Gestion des véhicules
- Ajout et modification des véhicules
- Association des véhicules aux conducteurs
- Suppression d'un véhicule non utilisé dans un trajet

### Gestion des trajets
- Publication d'un trajet avec plusieurs points d'arrêt
- Consultation des trajets
- Gestion des statuts : `PUBLIE`, `COMPLET`, `TERMINE`, `ANNULE`
- Calcul des places restantes

### Recherche des trajets
- Recherche par ville de départ et d'arrivée
- Filtrage par date
- Filtrage par prix maximal et nombre de places
- Pagination et tri
- Calcul du prix entre deux points d'arrêt

### Gestion des réservations
- Création d'une réservation
- Calcul automatique du montant total
- Vérification des places disponibles
- Consultation et annulation des réservations
- Mise à jour automatique du statut du trajet

## 📋 Règles métier

- Un conducteur ne peut utiliser que ses propres véhicules.
- Un trajet doit contenir au minimum deux points d'arrêt.
- Les horaires des points d'arrêt doivent être croissants.
- Une ville ne peut apparaître qu'une seule fois dans un trajet.
- Les prix doivent être croissants, avec un prix initial égal à zéro.
- Une réservation est possible uniquement sur un trajet publié.
- Une place réservée est occupée sur l'ensemble du trajet.
- Un passager ne peut avoir qu'une réservation confirmée par trajet.
- L'annulation d'une réservation doit respecter le délai de 24 heures avant le départ.
- Le montant d'une réservation est calculé selon le segment parcouru et le nombre de places.

## 🚀 Installation et démarrage

### Prérequis

- JDK 17 ou supérieur
- Docker Desktop
- Maven (ou Maven Wrapper)
- Git

### 1. Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
cd covoiturage-api
```

### 2. Démarrer PostgreSQL

```bash
docker compose up -d
```

### 3. Lancer l'application

Sous Windows :

```bash
.\mvnw.cmd spring-boot:run
```

Sous Linux / macOS :

```bash
./mvnw spring-boot:run
```

## 🌐 API REST

Quelques endpoints principaux :

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/trajets/{id}` | Détail d'un trajet |
| GET | `/api/trajets/recherche` | Recherche des trajets |
| GET | `/api/trajets/conducteur/{id}` | Trajets d'un conducteur |
| POST | `/api/reservations` | Créer une réservation |
| GET | `/api/reservations/passager/{id}` | Réservations d'un passager |
| GET | `/api/reservations/trajet/{id}` | Réservations d'un trajet |
| PUT | `/api/reservations/{id}/annuler` | Annuler une réservation |

### Exemple de recherche

```http
GET /api/trajets/recherche?villeDepart=Oujda&villeArrivee=Nador&dateDepart=2026-12-01&prixMax=100&placesMin=1&page=0&size=10
```

Cette requête recherche les trajets disponibles entre Oujda et Nador le 1er décembre 2026, avec un prix maximal de 100 DH et au moins une place disponible.

## ⚡ Optimisation des performances

- Utilisation de **JOIN FETCH** pour éviter le problème N+1.
- Requêtes JPQL pour la recherche des trajets.
- Calcul groupé des places réservées avec `SUM` et `GROUP BY`.
- Pagination avec Spring Data JPA.
- Chargement optimisé des relations entre entités.

## 🛡️ Gestion des erreurs

Les exceptions sont centralisées avec `GlobalExceptionHandler` et retournées au format `ProblemDetail`.

Principaux codes HTTP utilisés :

- `400` : Requête invalide
- `403` : Action interdite
- `404` : Ressource introuvable
- `409` : Conflit avec une règle métier
- `500` : Erreur interne du serveur

## 🧪 Tests

Les tests unitaires utilisent **JUnit 5** et **Mockito**.

Pour exécuter les tests :

```bash
.\mvnw.cmd test
```

Pour vérifier le projet complet :

```bash
.\mvnw.cmd clean verify
```

## 👨‍💻 Auteur

**Badr Fenni**

Développeur Full-Stack — YouCode

GitHub : https://github.com/
