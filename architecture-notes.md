# ANALYSE APPLICATION BACK END

## 1. Architecture générale

### 1.1 Stack technique

- **Framework** : Spring Boot 3 (Java 21)
- **Sécurité** : Spring Security + JWT
- **Persistance** : Spring Data JPA (Hibernate) + MySQL (via Docker Compose)
- **Build** : Maven

### 1.2 Découpage en couches

L’application respecte le découpage classique :

- **Controller (`controllers`)**
    - Expose les endpoints REST (`/api/**`)
    - Récupère les paramètres de requête / path / body
    - Appelle la couche service
    - Convertit entités / DTO via les mappers

- **Service métier (`services`)**
    - Contient la logique métier :
        - recherche d’entités,
        - vérification d’existence,
        - règles fonctionnelles (participation à une session, suppression de compte, etc.)
    - Appelle les repositories

- **Repository (`repository`)**
    - Interfaces Spring Data `JpaRepository`
    - Accès direct à la base de données

- **DTO & Mapper (`dto`, `mapper`)**
    - DTO exposés via l’API (`SessionDto`, `TeacherDto`, `UserDto`)
    - Mappers MapStruct (`SessionMapper`, `TeacherMapper`, `UserMapper`) pour transformer DTO / entités

- **Sécurité (`security`)**
    - `WebSecurityConfig` : configuration Spring Security (filtre JWT, endpoints protégés)
    - `UserDetailsServiceImpl` / `UserDetailsImpl` : intégration des utilisateurs applicatifs dans Spring Security
    - `JwtUtils`, `AuthTokenFilter`, `AuthEntryPointJwt` pour la gestion des JWT


---

## 2. Gestion des exceptions / erreurs

### Exceptions métier

Deux exceptions métier sont utilisées :

- `NotFoundException` (`@ResponseStatus(HttpStatus.NOT_FOUND)`)
- `BadRequestException` (`@ResponseStatus(HttpStatus.BAD_REQUEST)`)

Elles sont **levées depuis les services** lorsque :

- une ressource (session, user, teacher) n’existe pas (`NotFoundException`)
- une règle métier n’est pas respectée (ex : double participation) (`BadRequestException`)

Grâce à l’annotation `@ResponseStatus`, Spring traduit automatiquement ces exceptions en réponses HTTP 404/400, ce qui :

- supprime la duplication de code `if (x == null) return 404` dans les controllers ;
- évite les `try/catch` répétitifs pour gérer les cas d’erreur courants.

---

## 3. Respect du découpage Controller -> Service -> Repository

### 3.1 Session (`SessionController` / `SessionService` / `SessionRepository`)

#### Observés dans le code initial

- `SessionController` :
    - parsait les IDs à la main (`String id` + `Long.parseLong(id)`),
    - gérait les `NumberFormatException` avec des `try/catch`,
    - faisait des tests `if (session == null) { return 404; }`.
- `SessionService` :
    - `getById` retournait `null` si la session n’existait pas,
    - `delete` supprimait directement par ID sans vérifier l’existence.

#### Corrections apportées

- **`SessionService`**
    - `getById(Long id)`  
      -> `sessionRepository.findById(id).orElseThrow(NotFoundException::new);`
    - `delete(Long id)`  
      -> charge d’abord la session puis la supprime, et lève `NotFoundException` si elle n’existe pas.
    - `update(Long id, Session session)`  
      -> vérifie l’existence avant de sauvegarder, sinon `NotFoundException`.
    - `participate(Long id, Long userId)` / `noLongerParticipate(Long id, Long userId)`  
      -> contiennent la logique métier :
        - vérifier que session et user existent,
        - vérifier que l’utilisateur participe ou non déjà,
        - lever `BadRequestException` si la règle n’est pas respectée.

- **`SessionController`**
    - Utilise `@PathVariable Long id` au lieu de `String id` + `parseLong`.
    - Ne gère plus les `NumberFormatException` ni les `null` :
        - se contente d’appeler `sessionService`,
        - laisse les exceptions métier remonter (gérées via `@ResponseStatus`).
    - Reste focalisé sur :
        - les signatures d’API,
        - le mapping entité ⇆ DTO via `SessionMapper`,
        - les codes HTTP de succès (`200`, `201`, etc.).

---

### 3.2 User (`UserController` / `UserService` / `UserRepository`)

#### Observés dans le code initial

- `UserController` :
    - try/catch autour du parsing d’ID (`NumberFormatException`),
    - test `if (user == null)` pour renvoyer 404,
    - suppression de l’utilisateur via `userService.delete(id)` mais sans vérification d’existence dans le service.
    - logique métier d’autorisation (“l’utilisateur ne peut supprimer que son propre compte”) dans le controller.

- `UserService` :
    - `findById(Long id)` retournait `null` si non trouvé.
    - `delete(Long id)` supprimait directement par ID.

#### Corrections apportées

- **`UserService`**
    - `findById(Long id)`  
      -> `userRepository.findById(id).orElseThrow(NotFoundException::new);`
    - `delete(Long id)`  
      -> récupère l’utilisateur, lève `NotFoundException` si besoin, puis supprime.
    - `existsByEmail(String email)` et `save(User user)`  
      -> utilisés par la couche Auth pour `register`.
    - `findByEmail(String email)`  
      -> renvoie un `User` ou lève `NotFoundException`.

- **`UserController`**
    - `@PathVariable Long id` : plus de parsing manuel de String.
    - `findById` : appelle `userService.findById(id)` et renvoie le DTO.
    - `delete` :
        - récupère l’utilisateur via `userService.findById(id)`,
        - compare l’email de l’utilisateur connecté (via `SecurityContextHolder`) avec `user.getEmail()` pour sécuriser la suppression,
        - appelle `userService.delete(id)` si autorisé.

* La règle métier d’autorisation (seul l’utilisateur peut supprimer son propre compte) reste dans le controller par logique car elle s’appuie directement sur le contexte de sécurité HTTP.

---

### 3.3 Teacher (`TeacherController` / `TeacherService` / `TeacherRepository`)

- **`TeacherService`**
    - `findAll()` renvoie tous les enseignants.
    - `findById(Long id)` -> lève `NotFoundException` si l’enseignant n’existe pas.

- **`TeacherController`**
    - `findById(@PathVariable Long id)` -> appelle le service, renvoie un `TeacherDto`.
    - `findAll()` -> renvoie la liste des `TeacherDto`.
    - Plus de `try/catch` ni de `if (teacher == null)`.

* Le controller devient un adaptateur HTTP ; la logique d’existence est gérée par le service.

---

### 3.4 Auth (`AuthController` / `UserDetailsServiceImpl` / `UserService`)

#### Observés dans le code initial

- `AuthController` :
    - injectait directement `UserRepository` pour :
        - vérifier l’unicité de l’email,
        - recharger l’utilisateur et déterminer `isAdmin`.

- `UserDetailsServiceImpl` :
    - construisait `UserDetailsImpl` **sans renseigner le champ `admin`**.

#### Corrections apportées

- **`UserDetailsServiceImpl`**
    - charge l’utilisateur par email via `UserRepository`.
    - construit un `UserDetailsImpl` avec :
        - `id`, `username`, `firstName`, `lastName`, `password`,
        - **`admin`** correctement renseigné (`user.isAdmin()`).

- **`AuthController`**
    - n’utilise plus `UserRepository` directement.
    - dépend de `UserService` pour :
        - `existsByEmail(email)` (vérification avant inscription),
        - `save(user)` (persistance du nouvel utilisateur).
    - `/login` :
        - authentifie via `AuthenticationManager`,
        - génère un JWT via `JwtUtils`,
        - récupère les infos depuis `UserDetailsImpl`, y compris `admin`.
    - `/register` :
        - vérifie que l’email n’est pas déjà pris via `userService.existsByEmail`,
        - crée et sauvegarde un `User` via `userService.save`.

---

## 4. Mappers & DTO

### `SessionMapper`

`SessionMapper` gère le mapping complexe entre :

- `SessionDto` (côté API)
- `Session` (entité JPA avec associations `teacher` et `users`)

Principes :

- Lors du passage DTO -> entité (`toEntity`) :
    - `teacher_id` est converti en entité `Teacher` via `TeacherService.findById(...)`.
    - `users` (liste d’IDs) est convertie en liste d’entités `User` via `UserService.findById(...)`.
    - Si un ID est invalide, `NotFoundException` est levée par le service, ce qui entraîne une réponse 404.

- Lors du passage entité -> DTO (`toDto`) :
    - `session.teacher.id` est exposé en tant que `teacher_id`.
    - `session.users` est converti en liste de `Long` (IDs).

