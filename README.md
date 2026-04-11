# CredArc — Banking API

![Platform](https://img.shields.io/badge/Platform-Backend_API-009688?logo=fastapi&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)
![Framework](https://img.shields.io/badge/Framework-Spring_Boot-6DB33F?logo=springboot&logoColor=white)
![Security](https://img.shields.io/badge/Security-Spring_Security_+_JWT-6DB33F?logo=springsecurity&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql&logoColor=white)
![Docs](https://img.shields.io/badge/Docs-Swagger_UI-85EA2D?logo=swagger&logoColor=black)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.0.0-blue)

A backend-first banking system built with Java and Spring Boot, designed to simulate core financial operations with a focus on data integrity, atomic transactions, and production-grade security. Built as a portfolio project to demonstrate real-world API design, layered architecture, and secure authentication patterns.

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

- **JWT Authentication** : Stateless auth with BCrypt password hashing and 24hr token expiry
- **Default Account Provisioning** : Bank account automatically created on first login
- **Multi-Account Support** : Users can hold up to 2 accounts
- **Atomic Transfers** : Utilizes Spring’s `@Transactional` boundary to ensure that peer-to-peer transfers either succeed completely or roll back entirely, guaranteeing data integrity.
- **Overdraft Protection** : Balance validation enforced at service layer before any DB write
- **Ownership Enforcement** : Users can only operate on their own accounts; any violation returns 403
- **Optimistic Locking** : `@Version` field prevents concurrent modification corruption
- **Paginated Transaction History** : Efficient retrieval of transaction logs with configurable page size
- **Standardized Error Responses** : Global exception handler returns consistent JSON error structure
- **Swagger UI** : Interactive API documentation available at `/swagger-ui/index.html`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (JJWT) |
| ORM | JPA / Hibernate |
| Database | MySQL |
| Password Hashing | BCrypt |
| Documentation | Springdoc OpenAPI (Swagger UI) |
| Build Tool | Maven |

---

## Architecture

```
Client → Controller → Service → Repository → MySQL
```

- **Controller** — Handles HTTP requests, extracts authenticated user from `SecurityContext`
- **Service** — Business logic, validation, ownership enforcement
- **Repository** — Spring Data JPA interfaces
- **Security** — JWT filter chain intercepts every request before it reaches controllers

```
src/
├── config/          # SecurityConfig, OpenApiConfig
├── controller/      # AccountController, AuthController, TransactionController
├── dto/             # Request and Response DTOs
├── entity/          # JPA Entities (User, Account, Transaction)
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── repository/      # Spring Data JPA Repositories
├── security/        # JwtAuthFilter, JWTService, CustomUserDetails, CustomAuthEntryPoint
└── service/         # AccountService, AuthService, TransactionService, UserService
```

---

## API Endpoints

### Auth — Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/signup` | Register a new user |
| POST | `/auth/login` | Login and receive JWT token + account details |

### Accounts — Requires JWT
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/accounts/all` | Get all accounts for the logged-in user |
| POST | `/accounts/new` | Open a new account (max 2 per user) |

### Transactions — Requires JWT
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions/credit` | Credit an account |
| POST | `/transactions/debit` | Debit an account (ownership enforced) |
| POST | `/transactions/transfer` | Atomic transfer between two accounts (ownership enforced on sender) |
| GET | `/transactions/{accountId}?page=0&size=10` | Paginated transaction history |

### Other
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/health` | No | Health check |
| GET | `/swagger-ui/index.html` | No | Interactive API docs |

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable description"
}
```

| Error Code | Status | Cause |
|------------|--------|-------|
| `UNAUTHORIZED` | 401 | Missing or invalid JWT token |
| `ACCESS_DENIED` | 403 | Attempting to access another user's account |
| `ACCOUNT_NOT_FOUND` | 404 | Account ID does not exist |
| `INSUFFICIENT_BALANCE` | 412 | Not enough funds for debit or transfer |
| `DUPLICATE_USER` | 409 | Email or mobile already registered |
| `CONCURRENT_MODIFICATION` | 409 | Optimistic lock conflict — retry the request |
| `LIMIT_EXCEEDED` | 400 | Maximum 2 accounts per user reached |
| `INVALID_REQUEST` | 400 | Validation failure or bad input |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## Installation

### Prerequisites
- Java 21
- MySQL 8+
- Maven

### Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/credarc.git
cd credarc
```

2. Create the database:
```sql
CREATE DATABASE credarc;
```

3. Set environment variables:

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL e.g. `jdbc:mysql://localhost:3306/credarc` |
| `DB_PASSWORD` | Your MySQL password |
| `JWT_SECRET` | Base64 encoded secret key |

**In IntelliJ:** Run/Debug Configurations → Modify Options → Environment Variables
```
DB_URL=jdbc:mysql://localhost:3306/credarc;DB_PASSWORD=yourpassword;JWT_SECRET=yoursecret
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

API available at: `http://localhost:8080`

Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

---

## Usage

### 1. Signup
```json
POST /auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "password": "securepassword"
}
```

### 2. Login
```json
POST /auth/login
{
  "email": "john@example.com",
  "password": "securepassword"
}
```
Returns a JWT token and your account details. Use the token in all subsequent requests.

### 3. Authorize
Add to request headers:
```
Authorization: Bearer <your_token>
```

Or use the **Authorize** button in Swagger UI at `/swagger-ui/index.html`.

### 4. Transfer
```json
POST /transactions/transfer
{
  "fromAccountId": "your-account-uuid",
  "toAccountId": "recipient-account-uuid",
  "amount": 100.00
}
```

---

## Roadmap

### V2 — In Progress
- [ ] Short-lived access tokens (15 min) + refresh token rotation
- [ ] Docker + Docker Compose setup
- [ ] JUnit + Mockito test coverage targeting auth and transaction logic

### V3 — Planned
- [ ] Redis caching for account balance reads
- [ ] OAuth2 login (Google)
- [ ] Email notifications on transactions
- [ ] Kafka event streaming for transaction logs
- [ ] Role-based access control (ADMIN / USER)

---

## Contact

**Monit Bisht** - *Aspiring Android & Backend Developer*
- [GitHub Profile](https://github.com/monitbisht)
- [LinkedIn Profile](https://www.linkedin.com/in/monit-bisht-414318338/)
- Email: monitbisht15@gmail.com
