# Personal Finance Manager

A production-grade RESTful Personal Finance Management backend built with **Spring Boot 3.3.5**, **Java 21**, **Spring Security**, and **MySQL**.

---

## Features
- **User Management & Authentication**: Session-based cookie authentication (`JSESSIONID`) with BCrypt password hashing and custom JSON 401/403 error responses.
- **Strict Data Isolation**: Complete multi-tenant data segregation ensuring users can only access their own transactions, categories, goals, and reports.
- **Predefined & Custom Categories**:
  - Default seeded categories: `Salary` (Income), `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities` (Expenses).
  - Custom category creation with per-user uniqueness and deletion protection when referenced by transactions.
- **Transaction Tracking**:
  - Income and expense transactions with amount, date ($\le$ today), category, and description.
  - Multi-criteria filtering (date range, category, type) sorted by newest first.
  - Date immutability on update and soft deletion.
- **Savings Goals**:
  - Dynamic real-time calculation of savings progress: `(Total Income - Total Expenses)` from goal start date.
  - Automatic computation of progress percentage and remaining amount.
- **Reports & Analytics**:
  - Monthly breakdown of income/expenses grouped by category and net savings.
  - Yearly aggregation across all 12 months.
- **Error Handling**: Standardized RFC-compliant JSON error responses mapping accurately to `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, and `409 Conflict`.

---

## Technology Stack
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Security**: Spring Security (Session Cookie Auth, BCrypt)
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: MySQL (Production/Local), H2 (In-memory Test Profile)
- **Validation**: Jakarta Validation (`jakarta.validation`)
- **Testing**: JUnit 5, Mockito, Spring Security Test, Spring Test MockMvc
- **Build Tool**: Apache Maven

---

## API Specification

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user account | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `POST` | `/api/auth/login` | Authenticate and obtain session cookie | `200 OK`, `401 Unauthorized` |
| `POST` | `/api/auth/logout` | Invalidate session | `200 OK`, `401 Unauthorized` |

#### Register Request
```json
POST /api/auth/register
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

#### Login Request
```json
POST /api/auth/login
{
  "username": "user@example.com",
  "password": "password123"
}
```

---

### 2. Category Management (`/api/categories`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/categories` | List default + user custom categories | `200 OK`, `401 Unauthorized` |
| `POST` | `/api/categories` | Create custom category | `201 Created`, `400 Bad Request`, `401 Unauthorized`, `409 Conflict` |
| `DELETE` | `/api/categories/{name}` | Delete custom category | `200 OK`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |

#### Create Custom Category
```json
POST /api/categories
{
  "name": "SideBusinessIncome",
  "type": "INCOME"
}
```

---

### 3. Transaction Management (`/api/transactions`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/transactions` | Create income or expense transaction | `201 Created`, `400 Bad Request`, `401 Unauthorized` |
| `GET` | `/api/transactions` | Get transactions with optional filters (`?startDate=...&endDate=...&categoryId=...&type=...`) | `200 OK`, `401 Unauthorized` |
| `PUT` | `/api/transactions/{id}` | Update transaction (**date cannot be modified**) | `200 OK`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `DELETE` | `/api/transactions/{id}` | Soft delete transaction | `200 OK`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |

#### Create Transaction
```json
POST /api/transactions
{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```

---

### 4. Savings Goals (`/api/goals`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/goals` | Create savings goal | `201 Created`, `400 Bad Request`, `401 Unauthorized` |
| `GET` | `/api/goals` | List all savings goals with calculated progress | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/goals/{id}` | Get goal by ID | `200 OK`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `PUT` | `/api/goals/{id}` | Update target amount and target date | `200 OK`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `DELETE` | `/api/goals/{id}` | Delete goal | `200 OK`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |

#### Create Goal
```json
POST /api/goals
{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-01-01",
  "startDate": "2025-01-01"
}
```

---

### 5. Reports and Analytics (`/api/reports`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/reports/monthly/{year}/{month}` | Monthly breakdown by category & net savings | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/reports/yearly/{year}` | Yearly aggregated financial overview | `200 OK`, `401 Unauthorized` |

---

## Running Locally

### Prerequisites
- **Java 21+**
- **Maven 3.9+** (or use included `./mvnw`)
- **MySQL 8.0+** running locally on port 3306 (or configured via environment variables)

### Configuration
Set MySQL credentials in `src/main/resources/application.properties` or via environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=finance_db
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

### Build & Run
```bash
mvn clean package
java -jar target/Finance_Manager-0.0.1-SNAPSHOT.jar
```
Or directly with Spring Boot plugin:
```bash
mvn spring-boot:run
```

---

## Running Tests
Run the complete automated test suite (Unit & Integration tests):
```bash
mvn clean test
```

---

## Docker & Cloud Deployment (Render)

### Build Docker Image
```bash
docker build -t personal-finance-manager .
docker run -p 8080:8080 -e DB_HOST=... -e DB_USERNAME=... -e DB_PASSWORD=... personal-finance-manager
```

### Render Deployment
This repository includes a [`render.yaml`](render.yaml) file for automatic deployment on Render using the included multi-stage [`Dockerfile`](Dockerfile).
