# Test de la Gestion des Interventions

## Fichiers Frontend Créés

### Modèles
- `src/app/models/demande.model.ts` - Interface Demande
- `src/app/models/intervention.model.ts` - Interface Intervention

### Services
- `src/app/services/api.service.ts` - Service API pour communiquer avec le backend

### Composants
- `src/app/components/demande-list/` - Liste et planification des demandes
- `src/app/components/intervention-list/` - Liste et gestion des interventions

### Routes
Les routes suivantes ont été ajoutées :
- `/demandes` - Liste des demandes citoyennes
- `/interventions` - Liste des interventions

## Comment Tester

### 1. Démarrer le Backend
```bash
cd "/Users/eyadammak/Documents/CYCLE ING/SGII-Ville"
mvn spring-boot:run
```

Le backend démarre sur http://localhost:8080

### 2. Démarrer le Frontend
```bash
cd "/Users/eyadammak/Documents/CYCLE ING/SGIIVILLE-FE"
ng serve
```

Le frontend démarre sur http://localhost:4200

### 3. Accéder à l'application

1. **Se connecter** : http://localhost:4200/login
   - Utilisez un compte existant (ex: admin@sgii.tn / eya@gmail.com)

2. **Voir les demandes** : http://localhost:4200/demandes
   - Vous verrez 3 demandes de test
   - Cliquez sur "Planifier" pour créer une intervention

3. **Voir les interventions** : http://localhost:4200/interventions
   - Vous verrez les interventions planifiées
   - Cliquez sur "Démarrer" pour passer en EN_COURS
   - Cliquez sur "Terminer" pour passer en TERMINEE

## Flux de Test Complet

1. ✅ Connexion avec un compte admin/chef de service
2. ✅ Navigation vers `/demandes`
3. ✅ Clic sur "Planifier" pour une demande
4. ✅ Confirmation dans le modal
5. ✅ L'intervention est créée automatiquement
6. ✅ Navigation vers `/interventions`
7. ✅ L'intervention apparaît avec priorité URGENTE et état EN_ATTENTE
8. ✅ Clic sur "Démarrer" → état passe à EN_COURS
9. ✅ Clic sur "Terminer" → état passe à TERMINEE

## Données de Test

Le fichier `demandes.xml` contient 3 demandes :
1. Réparation d'un lampadaire (SOUMISE)
2. Nettoyage d'un parc (SOUMISE)
3. Fuite d'eau (EN_ATTENTE)

## APIs Backend Utilisées

- `GET /api/demandes` - Liste des demandes
- `POST /api/demandes/planifier/{id}` - Planifier une intervention
- `GET /api/interventions` - Liste des interventions
- `PATCH /api/interventions/{id}` - Mettre à jour le statut (body: {statut: "EN_COURS"})

## Navigation dans l'Application

Pour tester facilement, ajoutez des liens de navigation dans votre composant `home.component.html` :

```html
<nav>
  <a routerLink="/demandes">Demandes</a>
  <a routerLink="/interventions">Interventions</a>
</nav>
```
