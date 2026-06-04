# 🏥 HopitalGest

> Système de gestion hospitalière desktop — JavaFX 21 · MySQL · Architecture MVC

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Termine-yellow?style=flat-square)

---

## 📋 Table des matières

- [Aperçu](#-aperçu)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Structure du projet](#-structure-du-projet)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration base de données](#-configuration-base-de-données)
- [Rôles et accès](#-rôles-et-accès)
- [Technologies utilisées](#-technologies-utilisées)
- [Auteur](#-auteur)

---

## 🌟 Aperçu

**HopitalGest** est une application desktop de gestion hospitalière développée en **JavaFX** avec une base de données **MySQL**. Elle permet de centraliser et gérer l'ensemble des activités d'un établissement de santé : patients, médecins, pharmaciens et administrateurs disposent chacun d'un espace personnalisé adapté à leurs besoins.

---

## ✨ Fonctionnalités

### 🔐 Authentification
- Connexion sécurisée avec hashage **SHA-256** des mots de passe
- Redirection automatique selon le rôle de l'utilisateur
- Validation des champs en temps réel
- Protection contre les injections SQL via `PreparedStatement`

---

### 👤 Espace Patient
| Fonctionnalité | Description |
|---|---|
| 📋 Dossier médical | Consultation du dossier complet : groupe sanguin, allergies, antécédents |
| 🕒 Historique | Historique complet des consultations avec diagnostic et notes |
| 📄 Ordonnances | Visualisation des ordonnances liées à chaque consultation |
| 📅 Rendez-vous | Suivi des rendez-vous médicaux |
| 💳 Factures | Consultation des factures générées |

---

### 🩺 Espace Médecin
| Fonctionnalité | Description |
|---|---|
| 📅 Gestion des RDV | Acceptation / refus des rendez-vous patients |
| 👥 Dossiers patients | Accès aux dossiers médicaux complets |
| 📝 Ordonnance | Rédaction d'ordonnances avec lignes de médicaments |
| 🗓 Planning | Vue calendrier hebdomadaire avec navigation |
| 💰 Facturation | Génération de factures avec calcul automatique |

---

### 💊 Espace Pharmacien
| Fonctionnalité | Description |
|---|---|
| 📦 Stock | Gestion complète des médicaments avec alertes de rupture |
| 💊 Dispensation | Dispensation sur ordonnance avec déduction automatique du stock |
| 📋 Ordonnances | Suivi et mise à jour des statuts d'ordonnances |

---

### 🔧 Espace Administrateur
| Fonctionnalité | Description |
|---|---|
| 👤 Utilisateurs | CRUD complet : ajout, modification, suppression, réinitialisation MDP |
| 📊 Rapports | Statistiques globales par rôle et activité |
| 💳 Paiements | Vue d'ensemble des paiements et factures |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│                   VIEW (FXML)                   │
│         Interfaces utilisateur JavaFX           │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│               CONTROLLER (Java)                 │
│    Logique métier · Validation · Navigation     │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│                  DAO (Java)                     │
│         Accès données · PreparedStatement       │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│              BASE DE DONNÉES MySQL              │
│    Utilisateur · Patient · Medecin · ...        │
└─────────────────────────────────────────────────┘
```

### Héritage du modèle

```
Utilisateur (classe parente)
├── Medecin
├── Patient
├── Pharmacien
└── Admin
```

---

## 📁 Structure du projet

```
HopitalGest/
├── src/
│   └── application/
│       ├── MainApp.java
│       ├── model/
│       │   ├── Utilisateur.java
│       │   ├── Medecin.java
│       │   ├── Patient.java
│       │   ├── Pharmacien.java
│       │   ├── Admin.java
│       │   ├── Consultation.java
│       │   ├── Medicament.java
│       │   ├── Ordonnance.java
│       │   └── LigneOrdonnance.java
│       ├── dao/
│       │   ├── DatabaseConnection.java
│       │   ├── UtilisateurDAO.java
│       │   ├── MedecinDAO.java
│       │   ├── AdminDAO.java
│       │   ├── PharmacienDAO.java
│       │   ├── OrdonnanceDAO.java
│       │   ├── ConsultationDAO.java
│       │   └── DossierMedicalDAO.java
│       ├── controller/
│       │   ├── LoginController.java
│       │   ├── AccueilController.java
│       │   ├── MedecinSubController.java       ← interface
│       │   ├── PatientSubController.java       ← interface
│       │   ├── PharmacienSubController.java    ← interface
│       │   ├── AdminSubController.java         ← interface
│       │   ├── MedecinRdvController.java
│       │   ├── MedecinDossiersController.java
│       │   ├── MedecinOrdonnanceController.java
│       │   ├── MedecinPlanningController.java
│       │   ├── MedecinFactureController.java
│       │   ├── PharmacienStockController.java
│       │   ├── PharmacienDispensationController.java
│       │   ├── PharmacienOrdonnancesController.java
│       │   ├── AdminUtilisateursController.java
│       │   ├── AdminRapportsController.java
│       │   ├── AdminPaiementsController.java
│       │   └── PatientDossierController.java
│       ├── view/
│       │   └── ViewManager.java
│       └── resources/
│           ├── login.fxml
│           ├── accueil.fxml
│           ├── medecin_rdv.fxml
│           ├── medecin_dossiers.fxml
│           ├── medecin_ordonnance.fxml
│           ├── medecin_planning.fxml
│           ├── medecin_facture.fxml
│           ├── pharmacien_stock.fxml
│           ├── pharmacien_dispensation.fxml
│           ├── pharmacien_ordonnances.fxml
│           ├── admin_utilisateurs.fxml
│           ├── admin_rapports.fxml
│           ├── admin_paiements.fxml
│           └── styles/
│               └── main.css
├── lib/
│   ├── javafx-sdk/
│   └── mysql-connector-j-8.0.33.jar
└── README.md
```

---

## ⚙️ Prérequis

| Outil | Version minimale |
|---|---|
| Java JDK | 17 ou 21 |
| JavaFX SDK | 21 |
| MySQL Server | 8.0 |
| IDE | Eclipse / IntelliJ IDEA |

---

## 🚀 Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/zineb-elgaout/Systeme-gestion-hospitaliere-JavaFX-MySQL.git
cd Systeme-gestion-hospitaliere-JavaFX-MySQL
```

### 2. Télécharger JavaFX SDK

Aller sur [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/)
→ Télécharger **JavaFX 21 SDK** pour votre OS
→ Décompresser dans `/lib/javafx-sdk/`

### 3. Configurer dans IntelliJ / Eclipse

**IntelliJ IDEA :**
```
File → Project Structure → Libraries → (+) → Java
→ Sélectionner : lib/javafx-sdk/lib/
→ Appliquer
```

**VM Options à ajouter :**
```
--module-path lib/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

### 4. Lancer l'application

Exécuter la classe principale :
```
application.MainApp
```

---

## 🗄 Configuration base de données

### 1. Créer la base de données

```sql
CREATE DATABASE hopital_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```



### 2. Modifier les credentials dans `DatabaseConnection.java`

```java
private static final String URL      = "jdbc:mysql://localhost:3306/hopital_db";
private static final String USER     = "root";
private static final String PASSWORD = "votre_mot_de_passe";
```

---

## 👥 Rôles et accès

| Rôle | Accès | Couleur |
|---|---|---|
| 🔵 **Patient** | Dossier médical, RDV, Factures | Bleu `#1f618d` |
| 🟦 **Médecin** | RDV, Dossiers, Ordonnances, Planning, Factures | Bleu foncé `#1a5276` |
| 🟢 **Pharmacien** | Stock, Dispensation, Ordonnances | Vert `#117a65` |
| 🟣 **Admin** | Gestion complète utilisateurs, Rapports, Paiements | Violet `#7d3c98` |

---

## 🛠 Technologies utilisées

| Technologie | Usage |
|---|---|
| **Java 21** | Langage principal |
| **JavaFX 21** | Interface graphique desktop |
| **FXML** | Déclaration des interfaces UI |
| **CSS JavaFX** | Stylisation de l'interface |
| **MySQL 8** | Base de données relationnelle |
| **JDBC** | Connexion Java ↔ MySQL |
| **SHA-256** | Hashage des mots de passe |
| **MVC** | Pattern architectural |



---

*HopitalGest — Système de gestion hospitalière · JavaFX · MySQL*
