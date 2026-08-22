# CredArc — Banking API

![Platform](https://img.shields.io/badge/Platform-Backend_API-009688?logo=fastapi&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)
![Framework](https://img.shields.io/badge/Framework-Spring_Boot-6DB33F?logo=springboot&logoColor=white)
![Security](https://img.shields.io/badge/Security-Spring_Security_+_JWT-6DB33F?logo=springsecurity&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql&logoColor=white)
![Cache](https://img.shields.io/badge/Cache-Redis-DC382D?logo=redis&logoColor=white)
![Docs](https://img.shields.io/badge/Docs-Swagger_UI-85EA2D?logo=swagger&logoColor=black)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![Containerized](https://img.shields.io/badge/Containerized-Docker-2496ED?logo=docker&logoColor=white)
![Version](https://img.shields.io/badge/Version-2.1.0-blue)

A backend-first banking system built with Java and Spring Boot, designed to simulate core financial operations with a focus on data integrity, atomic transactions, and production-grade security. Built as a portfolio project to demonstrate real-world API design, layered architecture, secure authentication patterns, caching/rate-limiting strategy, and containerized deployment.

---

## Table of Contents

- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Error Responses](#error-responses)
- [Installation](#installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contact](#contact)

---

## Key Features

- **JWT Authentication**: Stateless auth with BCrypt password hashing, 15-min access tokens, and 7-day refresh tokens with rotation
- **Refresh Token Rotation & Replay Detection**: Refresh tokens are single-use and rotate on every refresh. Reuse of an already-rotated token revokes all active sessions for that user.
- **Redis Caching & Rate Limiting**: Account balance reads are cached (`@Cacheable`, evicted on debit/credit/transfer, 10-min TTL safety net); fixed-window rate limiting (`INCR` + `EXPIRE`) protects login and transaction endpoints per-user and per-IP
- **Atomic Transfers with Optimistic Locking**: `@Transactional` fund transfers either fully succeed or roll back, with overdraft protection and a `@Version` field guarding against concurrent balance corruption
- **Ownership Enforcement**: Users can only operate on their own accounts; any violation returns 403
- **Multi-Account Support**: Up to 2 accounts per user, with a default account auto-provisioned on first login
- **Paginated Transaction History**: Configurable page-size retrieval of transaction logs
- **Swagger UI**: Interactive API documentation at `/swagger-ui/index.html`
- **Containerized with Docker**: Multi-stage build and Docker Compose orchestration (app + MySQL + Redis) for a one-command, reproducible setup

---

## Tech Stack

| Layer             | Technology                      |
| ----------------- | -------------------------------- |
| Language           | Java 21                          |
| Framework          | Spring Boot 3.x                  |
| Security           | Spring Security + JWT (JJWT)     |
| ORM                | JPA / Hibernate                  |
| Database           | MySQL 8.4                        |
| Cache / Rate Limit | Redis                            |
| Password Hashing   | BCrypt                           |
| Documentation      | Springdoc OpenAPI (Swagger UI)   |
| Build Tool         | Maven                            |
| Containerization   | Docker + Docker Compose          |

---

## Architecture

```
Client → Controller → Service → Repository → MySQL
                            │
                            └→ Redis (cache reads, rate-limit counters)
```

- **Controller** — Handles HTTP requests, extracts authenticated user from `SecurityContext`
- **Service** — Business logic, validation, ownership enforcement, caching, rate limiting
- **Repository** — Spring Data JPA interfaces
- **Security** — JWT filter chain intercepts every request before it reaches controllers

```
src/
├── config/          # SecurityConfig, OpenApiConfig, RedisConfig
├── controller/      # AccountController, AuthController, TransactionController, HealthCheck
├── dto/             # Request and Response DTOs
├── entity/          # JPA Entities (User, Account, Transaction, RefreshToken)
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── redis/           # CacheEvictionService, RateLimiterService
├── repository/      # Spring Data JPA Repositories
├── security/        # JwtAuthFilter, JWTService, 
                     # CustomUserDetails, CustomUserDetailsService, CustomAuthEntryPoint
├── service/         # AccountService, AuthService, RefreshTokenService, 
│                    # PasswordService, TransactionService, UserService
├── utils/           # IpUtils
└── CredarcApplication.java
```

**Containerized architecture:**

```
docker compose up
        │
        ├── mysql service   (MySQL 8.4, healthcheck-gated)
        │
        ├── redis service   (Redis, caching + rate-limit counters)
        │
        └── app service     (multi-stage build: Maven+JDK → JRE runtime)
                │
                └── waits for mysql to report healthy before starting
```

All services run in isolated containers on a shared Docker Compose network, communicating over internal hostnames (`mysql`, `redis`) - no manual network setup required. MySQL data is persisted in a named Docker volume, so it survives `docker compose down` and container rebuilds.

**Dual-profile support:** the app can run two ways, each with its own datasource/cache configuration:
- **Docker** (`application-docker.properties`) : connects to `mysql:3306` and `redis:6379`, activated automatically via `SPRING_PROFILES_ACTIVE=docker` in Compose
- **Local / IntelliJ** (`application-local.properties`) : connects to `localhost:3306` and `localhost:6379` against native installs, activated via IntelliJ's Active Profiles run setting

This lets you run against native MySQL/Redis installs for fast local iteration, or fully containerized for a production-realistic test, without editing config by hand.

---

## API Endpoints

### Auth : Public

| Method | Endpoint       | Description                                   |
| ------ | -------------- | ---------------------------------------------- |
| POST   | `/auth/signup` | Register a new user                            |
| POST   | `/auth/login`  | Login and receive access + refresh tokens plus account details |
| POST   | `/auth/refresh` | Exchange a valid refresh token for a new access + refresh token pair |
| POST   | `/auth/logout` | Revoke a refresh token, ending that session |

### Accounts : Requires JWT

| Method | Endpoint        | Description                              |
| ------ | --------------- | ------------------------------------------ |
| GET    | `/accounts/all` | Get all accounts for the logged-in user (Redis-cached) |
| POST   | `/accounts/new` | Open a new account (max 2 per user)       |

### Transactions : Requires JWT

| Method | Endpoint                                    | Description                                                          |
| ------ | -------------------------------------------- | ---------------------------------------------------------------------- |
| POST   | `/transactions/credit`                       | Credit an account                                                     |
| POST   | `/transactions/debit`                        | Debit an account (ownership enforced, rate-limited)                   |
| POST   | `/transactions/transfer`                     | Atomic transfer between two accounts (ownership enforced on sender, rate-limited) |
| GET    | `/transactions/{accountId}?page=0&size=10`   | Paginated transaction history                                         |

### Other

| Method | Endpoint                  | Auth | Description            |
| ------ | -------------------------- | ---- | ------------------------ |
| GET    | `/health`                  | No   | Health check            |
| GET    | `/swagger-ui/index.html`   | No   | Interactive API docs    |

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable description"
}
```

| Error Code                | Status | Cause                                                                                                                                                          |
| -------------------------- | ------ |----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `UNAUTHORIZED`              | 401    | Missing or invalid JWT token                                                                                                                                   |
| `ACCESS_DENIED`             | 403    | Attempting to access another user's account                                                                                                                    |
| `ACCOUNT_NOT_FOUND`         | 404    | Account ID does not exist                                                                                                                                      |
| `INSUFFICIENT_BALANCE`      | 412    | Not enough funds for debit or transfer                                                                                                                         |
| `DUPLICATE_USER`            | 409    | Email or mobile already registered                                                                                                                             |
| `CONCURRENT_MODIFICATION`   | 409    | Optimistic lock conflict - retry the request                                                                                                                   |
| `LIMIT_EXCEEDED`            | 400    | Maximum 2 accounts per user reached                                                                                                                            |
| `RATE_LIMIT_EXCEEDED`       | 429    | Too many requests for login or transaction endpoint within the time window                                                                                     |
| `INVALID_REQUEST`           | 400    | Validation failure or bad input                                                                                                                                |
| `TOKEN_EXPIRED`             | 401    | Refresh token has expired                                                                                                                                      |
| `TOKEN_NOT_FOUND`           | 401    | Refresh token not recognized                                                                                                                                   |
| `WRONG_TOKEN_TYPE`          | 401    | An access token was submitted where a refresh token was expected                                                                                               |
| `TOKEN_REUSE_DETECTED`      | 401    | An already-used (rotated-out) or concurrently-claimed refresh token was resubmitted — signals possible theft or a race; all sessions for that user are revoked |
| `INTERNAL_ERROR`            | 500    | Unexpected server error                                                                                                                                        |

---

## Installation

### Option A : Docker (recommended)

This is the fastest way to run CredArc (no local Java, Maven, MySQL, or Redis installation required).

**Prerequisites:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

**Steps:**

1. Clone the repository:
   ```
   git clone https://github.com/monitbisht/CredArc-Backend.git
   cd CredArc-Backend
   ```

2. Copy the example environment file and fill in your own values:
   ```
   cp .env.example .env
   ```
   Then open `.env` and set:
   ```
   DB_PASSWORD=your_mysql_password
   JWT_SECRET=your_base64_encoded_secret
   ```

3. Build and start the app, database, and cache with one command:
   ```
   docker compose up --build
   ```
   This builds the Spring Boot app image, pulls MySQL 8.4 and Redis, waits for MySQL to become healthy, and starts the API automatically.

4. API available at: `http://localhost:8080`

   Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

To stop everything (data is preserved):
```
docker compose down
```

To stop everything **and** wipe the database (fresh start):
```
docker compose down -v
```

---

### Option B : Manual setup (without Docker)

**Prerequisites:**
- Java 21
- MySQL 8+
- Redis
- Maven

**Steps:**

1. Clone the repository:
   ```
   git clone https://github.com/monitbisht/CredArc-Backend.git
   cd CredArc-Backend
   ```

2. Create the database:
   ```sql
   CREATE DATABASE credarc;
   ```

3. Set environment variables:

   | Variable      | Description                          |
            | ------------- | -------------------------------------- |
   | `DB_PASSWORD` | Your MySQL password                    |
   | `JWT_SECRET`  | Base64 encoded secret key              |

   **In IntelliJ:** Run/Debug Configurations → Modify Options → Environment Variables
   ```
   DB_PASSWORD=yourpassword;JWT_SECRET=yoursecret
   ```

4. Ensure Redis is running locally on the default port (`6379`).

5. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

   API available at: `http://localhost:8080`

   Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

---

## Usage

### 1. Signup

```
POST /auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "password": "securepassword"
}
```

### 2. Login

```
POST /auth/login
{
  "email": "john@example.com",
  "password": "securepassword"
}
```

Returns an access token (15 min) and a refresh token (7 days), plus your account details. Use the access token in all subsequent requests.

### 3. Refresh

When your access token expires, exchange your refresh token for a new pair:

```
POST /auth/refresh
{
  "refreshToken": "your-refresh-token"
}
```

Returns a new access token and refresh token. The old refresh token is invalidated — save the new one for the next refresh.

### 4. Logout

```
POST /auth/logout
{
  "refreshToken": "your-refresh-token"
}
```

Revokes the refresh token, ending that session.

### 5. Authorize

Add to request headers:

```
Authorization: Bearer <your_token>
```

Or use the **Authorize** button in Swagger UI at `/swagger-ui/index.html`.

### 6. Transfer

```
POST /transactions/transfer
{
  "fromAccountId": "your-account-uuid",
  "toAccountId": "recipient-account-uuid",
  "amount": 100.00
}
```

---

## Roadmap

### V2 : Completed

- [x] Docker + Docker Compose setup
- [x] Persistent MySQL data via Docker volumes
- [x] JUnit + Mockito test coverage across all service classes
- [x] Short-lived access tokens (15 min) + refresh token rotation

### V3 : In Progress

- [x] Redis caching (getAllAccounts endpoint) and rate limiting (login + transaction endpoints)
- [ ] Frontend UI (wired to existing REST APIs) + deployment
- [ ] OAuth2 login (Google)
- [ ] Role-based access control (ADMIN / USER)
- [ ] Email notifications on transactions

---

## Contact

**Monit Bisht** - *Aspiring Backend Developer*

- [GitHub Profile](https://github.com/monitbisht)
- [LinkedIn Profile](https://www.linkedin.com/in/monit-bisht-414318338/)
- Email: monitbisht15@gmail.com