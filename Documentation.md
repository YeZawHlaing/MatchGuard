## Controllers (HTTP Request Layer)
   Controllers act as the entry point for incoming requests from the Flutter mobile application, parsing parameters, validating inputs, and routing payloads to the appropriate service layers.

---

### AuthController

**Base Route:** `/api/auth`

**Description:** Handles user onboarding and stateless authentication.

**Endpoints:**
```http request
POST /api/auth/register — Registers a new user account (CUSTOMER or SELLER). 

POST /api/auth/login — Authenticates credentials and issues a JWT token.
```
---

### ProductController

**Base Route:** `/api/products`

**Description:** Manages product creation, AI scam auditing, and conversational search.

**Endpoints:**
```http request
POST /api/products — Accepts new product listings and triggers the AI scam detection pipeline.

GET /api/products/search?query=... — Executes natural language semantic search, returning sorted, safety-scored product recommendations.

GET /api/products/seller/{sellerId} — Fetches all social media inventory listings belonging to a specific shop owner.
```
---

### TransactionController

**Base Route:** `/api/transactions`

**Description:** Manages the escrow lifecycle, receipt OCR verification, QR generation, and release handshakes.

**Endpoints:**
```http request

POST /api/transactions/checkout — Captures order details and payment proof screenshots, initiating AI OCR verification.

GET /api/transactions/seller/{sellerId} — Fetches incoming secured escrow orders for the seller's dashboard.

GET /api/transactions/{transactionId}/qr — Programmatically generates and returns a cryptographic QR code image/payload for an active escrow session.

POST /api/transactions/release — Validates scanned QR tokens during meetups, updates status to COMPLETED, and logs payout intent.

POST /api/transactions/cancel — Handles pre-meetup scam disputes or cancellations, triggering system refund states (CANCELLED_AND_REFUNDED).
```
---

## Service Classes (Business Logic & Integration Layer)
Services encapsulate the core domain logic, database operations, and external API integrations (such as LLM Vision/OCR and ZXing QR generation).

---

`AuthService`

**Responsibilities:**

- Validates unique email and phone constraints during registration.


- Encrypts user passwords securely using `BCryptPasswordEncoder`.


- Verifies credentials during login and generates signed JWT tokens via `JwtTokenProvider`.

---
`ProductService`

**Responsibilities:**

* Manages CRUD operations for product inventory using `ProductRepository`.
 

* Coordinates with `AiScamDetectionService` during product creation to auto-populate trust scores and safety flags.

---

`AiScamDetectionService`

**Responsibilities:**

* Interfaces with the external LLM API (Gemini/OpenAI) using structured prompt templates.
 

* Analyzes social listing descriptions, pricing anomalies, and high-risk phrasing to calculate a 0-100 `trust_score` and `is_verified_safe status`.

---

`AiSearchService`

**Responsibilities:**

* Accepts natural language user search parameters.


* Feeds inventory items and user intents into the LLM to perform semantic matching, prioritizing verified safe items and ranking results by fit score.
---

`TransactionService `

**Responsibilities:**

* Manages the strict escrow state machine (`PENDING_VERIFICATION` $\rightarrow$ `ESCROW_LOCKED` $\rightarrow$ `COMPLETED / CANCELLED_AND_REFUNDED`).


* Coordinates checkout initiation and refund request workflows.


* Validates QR token matches during the meetup handshake before clearing payouts.
---

`AiOcrService (Utility Service)`

**Responsibilities:**

* Processes uploaded payment voucher screenshots (KBZPay/Wave Money) via AI Vision APIs.


* Automatically extracts transaction amounts, sender phone numbers, and transaction IDs to auto-clear verification for escrow locking.

---

`QrCodeService (Utility Service)`

**Responsibilities:**

* Utilizes the Google ZXing library to encode transaction identifiers and secure cryptographic `qr_token` strings into scannable matrix barcodes.


* Converts generated QR matrices into streamable image formats or base64 payloads for the seller's mobile dashboard.

---