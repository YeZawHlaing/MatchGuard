## MatchGuard Backend API 

MatchGuard is an AI-driven search and escrow protocol designed to protect social commerce buyers and sellers (on Facebook, TikTok, and Viber) from scams. This backend is built with Spring Boot and provides secure authentication, AI-powered scam detection, semantic inventory search, automated receipt OCR verification, and a QR-code escrow release handshake.

---

### Documenation

| Documentation | Links to read                                                        |
|:--------------|:---------------------------------------------------------------------|
 | Documentation for spring architecture | https://github.com/YeZawHlaing/MatchGuard/blob/main/Documentation.md |
 | Github Workflow | https://github.com/YeZawHlaing/MatchGuard/blob/main/GitFlow.md                                                                     |

---
### Tech Stack

- **Language & Framework:** _Java 17+_, Spring Boot (Web, Data JPA, Spring Security, Validation)


- **Database:** PostgreSQL


- **AI Integration:** LLM API (Gemini / OpenAI) for semantic product search, product scam auditing, and receipt OCR validation.


- **Utilities:** Google ZXing (Zebra Crossing) library for backend QR code generation.


- **Build Tool:** Maven or Gradle

---
### Configuration (application.properties)

Configure your database credentials and AI API keys in your `src/main/resources/application.properties` file:
```properties

spring.datasource.url=jdbc:mysql://localhost:3306/matchguard_db
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

ai.api.key=your-gemini-or-openai-api-key

jwt.secret=your-super-secret-jwt-key-change-this-in-production
jwt.expiration=86400000

```
---
### How to Run the Project

Prerequisites
- Java Development Kit (JDK 17 or higher) installed.


- Maven installed (or use the included `./mvnw` wrapper).

#### Step-by-Step Instructions

1. Clone the repository:
```text
git clone https://github.com/your-username/matchguard-backend.git

cd matchguard-backend
```

2. Build the project dependencies
```
mvn clean install
```
3. Run the spring boot application
```text
mvn spring-boot:run
```
4. Server
   The server will start on http://localhost:8080. Database tables will be automatically created on startup via Hibernate DDL auto.
---

### API Endpoints Documentation

| Endpoint | Method | Portal\Role | Description | Request Parameters / Body | Success Response |
|:-|:-------|:-----|:-----------|:--------------------------|:-----------------|
| `/api/auth/register` | POST | Public | Register a new user account (Customer or Seller). | `name, email, phone, password, role` | 201 Created |
| `/api/auth/login` | POST | Public | Authenticate credentials and return a JWT token for mobile requests. | `email/phone, password` | 200 OK (Returns JWT + Profile) |
| `/api/products` | POST | Shop Owner | Register a product listing, triggering AI scam-detection analysis. | `sellerId, title, description, price, socialPostUrl` | 201 Created (Includes `trust_score` & safety flag) |
| `/api/products/search` | GET | Customer | AI semantic search matching requirements against inventory, prioritizing safe items. | Query Parameter: `?query=...` | 200 OK (JSON list + AI fit & safety score) |
| `/api/products/seller/{sellerId}` | GET | Shop Owner | Fetch all social media product listings registered by a specific shop owner. | Path Variable: `sellerId` | 200 OK (List of seller products) |
| `/api/transactions/checkout` | POST | Customer | Checkout with payment proof screenshot and phone number, setting status to pending verification. | `buyerId, productId, amount, senderPhone, screenshotUrl` | 201 Created (Status: `PENDING_VERIFICATION`) |
| `/api/transactions/seller/{sellerId}` | GET | Shop Owner | Fetch incoming secured escrow orders for the seller's dashboard. | Path Variable: `sellerId` | 200 OK (List of secured transactions) |
| `/api/transactions/{transactionId}/qr` | GET | Shop Owner | Generate a secure cryptographic QR code image or payload for the active escrow transaction. | Path Variable: `transactionId` | 200 OK (Returns QR image/base64 payload) |
| `/api/transactions/release` | POST | Handshake | Scan the backend-generated QR code during meetup/delivery to verify the token and instantly log payout intent. | `transactionId, qrToken` | 200 OK (Status: `COMPLETED`) |
| `/api/transactions/cancel` | POST | Customer | Request an escrow cancellation and refund if fraud is detected or before the QR scan meetup. | `transactionId, reason` | 200 OK (Status: `CANCELLED_AND_REFUNDED`) |





