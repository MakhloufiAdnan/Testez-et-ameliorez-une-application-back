# Spring Boot Quality Yoga API

Secure REST API for a yoga session management application built with **Java 21, Spring Boot and MySQL**.

This repository focuses on **backend quality, automated testing, security and code coverage**.

## ✨ Features

The API supports:

- user registration and authentication
- JWT-based authentication
- teacher management
- yoga session management
- user participation in sessions
- secured REST endpoints
- layered application architecture

## 🛠️ Tech Stack

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Maven
- Docker / Docker Compose
- JUnit 5
- MockMvc
- JaCoCo
- Postman

## 🏗️ Architecture

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
```

DTOs and mappers are used to separate the REST API layer from the persistence model.

Main packages include:

```text
src/main/java/com/openclassrooms/starterjwt/
├── configuration/
├── controllers/
├── dto/
├── exception/
├── mapper/
├── models/
├── payload/
├── repository/
├── security/
└── services/
```

## 🔐 Security

The application uses **Spring Security** with **JWT-based authentication**.

The security layer includes authentication-related components and protected API endpoints.

Environment-specific values are not committed to the repository.

A public `.env.example` file can be used as a reference for local configuration.

## 🧪 Testing Strategy

The project includes both **unit tests** and **integration tests**.

### Unit tests

Unit tests cover application logic such as:

- services
- security components
- utility classes

### Integration tests

Integration tests cover:

- REST controllers
- Spring Boot application context
- interactions between application layers

Run the full verification suite:

```bash
mvn clean verify
```

Run a specific unit test:

```bash
mvn -Dtest=UserServiceTest test
```

Run a controller integration test:

```bash
mvn -Dtest=SessionControllerTest test
```

## 📊 Code Coverage

Code coverage is measured with **JaCoCo**.

The Maven build enforces a minimum coverage threshold of **80%** on:

- instructions
- branches
- lines

The quality rule applies to packages containing application logic, including controllers, services and security components.

If the configured coverage thresholds are not met, the Maven build fails.

Generate the report with:

```bash
mvn clean test
```

The HTML report is generated at:

```text
target/site/jacoco/index.html
```

## 🐳 Database & Docker

The application uses **MySQL**.

A Docker Compose configuration is provided for local development.

Start the database:

```bash
docker compose up -d
```

Stop the containers:

```bash
docker compose down
```

## ▶️ Run Locally

### Requirements

- Java 21
- Maven 3.9+
- Docker
- Docker Compose
- Git

Clone the repository:

```bash
git clone https://github.com/MakhloufiAdnan/spring-boot-quality-yoga-api.git
cd spring-boot-quality-yoga-api
```

Create your local environment configuration from the example file:

```bash
cp .env.example .env
```

Then adapt the local values if necessary.

Start the MySQL container:

```bash
docker compose up -d
```

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

API:

```text
http://localhost:8080
```

## 🔎 API Testing

A Postman collection is available in the repository:

```text
postman/yoga.postman_collection.json
```

It can be used to test flows such as:

- login and registration
- user management
- session management
- participation in yoga sessions

## 📌 Project Focus

This repository demonstrates backend engineering practices around a Spring Boot application:

- REST API development
- layered architecture
- JWT authentication
- Spring Security
- persistence with MySQL
- unit testing
- controller integration testing
- automated quality verification
- JaCoCo coverage gates
- Docker-based local development
