# Yoga App !

Backend de l'application **Yoga App**.

Ce projet fournit une API REST sécurisée (JWT) pour gérer :

- les utilisateurs,
- les enseignants,
- les sessions de yoga,
- la participation des utilisateurs aux sessions.

L’architecture suit le découpage :

- `controller` -> `service` -> `repository`
- DTOs + mappers pour séparer l’API du modèle de persistance.
---

## Configuration du back

- **Nom du service** : `back`
- **Port HTTP** : `8080`

L’API sera accessible sur :

```text
http://localhost:8080
```

## Pré-requis pour le bon fonctionnement du back :

    -> JDK 21
    -> Docker
    -> Docker Compose
    -> Maven 3.9.3 (https://archive.apache.org/dist/maven/maven-3/3.9.3/binaries/) ou plus


## Installation & lancement de l’application

### Récupérer le projet

```
git clone https://github.com/MakhloufiAdnan/Testez-et-ameliorez-une-application-back.git
```

### Configuration de la base de données

Le projet utilise MySQL via Docker.
Un fichier compose.yaml est fourni à la racine.
Lancer la base MySQL :
```
docker compose up -d
```
Cela démarre un conteneur MySQL avec la configuration définie dans compose.yaml.

## Démarrage du back
Pour démarrer le back, il :
- démarrer Docker-Desktop sur votre poste de travail local.
- lancer une console, se placer à la racine du projet et exécuter la commande Maven :
```
mvn spring-boot:run
```
Cette commande va :
- initialiser le container Docker qui contient la base de données
- lancer l'application back et le connecter à la base de données précédemment créée

Les traces logs devraient ressemblées à ceci :
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.5)

[back] [           main] c.o.s.SpringBootSecurityJwtApplication   : Starting SpringBootSecurityJwtApplication using Java 21.0.3 with PID 15152
[back] [           main] c.o.s.SpringBootSecurityJwtApplication   : No active profile set, falling back to 1 default profile: "default"
[back] [           main] .s.b.d.c.l.DockerComposeLifecycleManager : Using Docker Compose file E:\dev\workspaces\missionOC\projet-p4\back\compose.yaml
[back] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container back_mysql  Created
[back] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container back_mysql  Starting
[back] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container back_mysql  Started
[back] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container back_mysql  Waiting
[back] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container back_mysql  Healthy
[back] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
[back] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 58 ms. Found 3 JPA repository interfaces.
[back] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
[back] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
[back] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.44]
[back] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
[back] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1360 ms
[back] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
[back] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.26.Final
[back] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
[back] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
[back] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
[back] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@16cb6f51
[back] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
[back] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 9.1
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
[back] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
[back] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
[back] [           main] eAuthenticationProviderManagerConfigurer : Global AuthenticationManager configured with AuthenticationProvider bean with name authenticationProvider
[back] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with an AuthenticationProvider bean. UserDetailsService beans will not be used by Spring Security for automatically configuring username/password login. Consider removing the AuthenticationProvider bean. Alternatively, consider using the UserDetailsService in a manually instantiated DaoAuthenticationProvider. If the current configuration is intentional, to turn off this warning, increase the logging level of 'org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer' to ERROR
[back] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
[back] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
[back] [           main] c.o.s.SpringBootSecurityJwtApplication   : Started SpringBootSecurityJwtApplication in 10.354 seconds (process running for 11.197)
```

Sur Docker-Desktop, vous devriez voir apparaître un container MySQL qui correspond au projet.

Vous pouvez vous connecter à la base de données et vérifier que la table ```USERS``` a été créée.
Pour cela, cliquez sur le lien `back_mysql` ce qui vous amènera sur la vue complète de la base de données.
Dans l'onglet ```Exec```, il faut :

1. se connecter à la base de données. Tapez la commande ci-dessous (les variables sont définies dans votre .env)
    ```
    mysql -u <nom_user> -p
    ```
   L'invite de commande demandera le mot de passe : ```<votre_password>```.

2. Se connecter au schéma de base de données `test`. Dans l'invite de commande, tapez la commande ci-dessous :
    ```
    USE test;
    ```

3. Copier le contenu du fichier `src/main/resources/sql/insert_user.sql` et l'exécuter dans l'invite de commande :
    ```
    INSERT INTO users(first_name, last_name, admin, email, password) VALUES ('Admin', 'Admin', true, 'yoga@studio.com', '$2a$10$.Hsa/ZjUVaHqi0tp9xieMeewrnZxrZ5pQRzddUXE/WjDu2ZThe6Iq');
    ```

4. Vérifier le contenu de la table `users`.
    ```
    SELECT * FROM users;
    ```
   Le résultat devrait afficher les données de l'utilisateur inséré précédemment.

   Ce script crée l'utilisateur admin par défaut :

    - login: yoga@studio.com
    - password: test!1234

## Ressources


### Collection Postman

Importez la collection Postman

> postman/yoga.postman_collection.json

La documentation de Postman se trouve ici :

https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-data-into-postman

## Utilisation

### Tester les requêtes :

- login / register,
- gestion des utilisateurs,
- gestion des sessions,
- participation aux sessions, etc.

### Lancer les tests

Les tests sont composés :

- de tests unitaires (services, sécurité, utilitaires),
- de tests d’intégration (controllers + contexte Spring Boot).

1. Lancer tous les tests

Depuis la racine du projet :
```
mvn clean verify
```
Cela va :
- lancer tous les tests JUnit 5 (unitaires + intégration),
- générer les rapports JaCoCo,
- vérifier le seuil de couverture configuré via `jacoco:check` (≥ 80 %).
  Rapport de couverture : `target/site/jacoco/index.html`.

2. Lancer un test spécifique

- Par exemple, pour ne lancer que les tests du UserService :
```
mvn -Dtest=UserServiceTest test
```
Pour un test d’intégration de controller :
```
mvn -Dtest=SessionControllerTest test
```
3. Structure du projet

```text
   src/
 ├── main/
 │   └── java/com/openclassrooms/starterjwt
 │       ├── configuration/
 │       ├── controllers/
 │       ├── dto/
 │       ├── exception/
 │       ├── mapper/
 │       ├── models/
 │       ├── payload/
 │       ├── repository/
 │       ├── security/
 │       └── services/
 └── test/
     └── java/com/openclassrooms/starterjwt
         ├── controllers/        # tests d’intégration des controllers
         ├── services/           # tests unitaires des services
         ├── security/           # tests JwtUtils, UserDetailsService, etc.
         └── SpringBootSecurityJwtApplicationTest.java  # test de contexte
```
4. Rapports de couverture (JaCoCo)

La couverture de code est mesurée avec JaCoCo, configuré dans le pom.xml.

- Générer le rapport

Le rapport est généré automatiquement lors de :
```
mvn clean test
```
- Où trouver le rapport

Après exécution :

Rapport HTML JaCoCo :

target/site/jacoco/index.html

- Seuil minimal de couverture

## Règles de couverture (seuil minimal 80%)

### Le build applique une règle JaCoCo ≥ 80% sur :

* INSTRUCTION
* BRANCH
* LINE

- La règle est appliquée sur les packages contenant de la logique :

com.openclassrooms.starterjwt.controllers

com.openclassrooms.starterjwt.services

com.openclassrooms.starterjwt.security

- Et les packages suivants sont exclus du calcul car considérés comme DTO / payload (exigence de l’énoncé) :

com.openclassrooms.starterjwt.dto

com.openclassrooms.starterjwt.payload.request

com.openclassrooms.starterjwt.payload.response

Si les seuils ne sont pas atteints, Maven échoue avec Coverage checks have not been met.