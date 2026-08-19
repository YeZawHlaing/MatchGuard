## MatchGuard: Escrow Checkout & AI OCR Verification API Documentation
This documentation covers the secure transaction workflow in MatchGua
rd, which includes customer checkout with receipt uploads, automated AI Vision OCR validation via Cloudflare R2 storage, and a manual seller approval fallback mechanism.

---
### **Overview of the Transaction Workflow**

1. **Customer Checkout** (`POST /api/transactions/checkout`):


- The customer uploads a payment screenshot (e.g., KPay/WavePay voucher) and submits payment details.


- The file is stored securely in Cloudflare R2.


- The backend invokes a Vision LLM (gpt-4o-mini) via Spring AI to automatically verify the receipt's amount and sender phone.


- If verification passes, the status becomes ESCROW_LOCKED and a cryptographic qrToken is generated. If the AI cannot read the receipt, it defaults to PENDING_VERIFICATION.

---

2. **Seller Dashboard** (`GET /api/transactions/seller/{sellerId}`):


- Sellers view all incoming orders, transaction statuses, and links to inspect uploaded receipt screenshots.


---

3. **Manual Seller Approval** (`POST /api/transactions/{transactionId}/approve/{sellerId}):
`
- If a receipt is marked as `PENDING_VERIFICATION`, the seller can manually inspect the R2 image and approve the transaction, locking the funds in escrow.

---

### 2. API Endpoints

**Endpoint 1:** Customer Checkout & Receipt Upload

`URL: /api/transactions/checkout`

Method:` POST`

Access: `Customer Role (ROLE_CUSTOMER)`

Content-Type: `multipart/form-data`

### Sample Response (200 OK) — If AI OCR fails or passes

#### request

| Key | Type                                                | Description                                    |
| :-- |:----------------------------------------------------|:-----------------------------------------------|
|productId,| Text,                                               | The database ID of the product being purchased |
buyerId,| Text,| The user ID of the customer                    |
amount,| Text,| The total transaction amount in MMK            |
senderPhone,| Text,| The buyer's wallet/transfer phone number       |
screenshot,| File,| "The payment transfer slip image (.jpg, .png)" |

#### response
```json
{
    "id": 1,
    "productId": 1,
    "productTitle": "Yamaha Sniper 150 - Used",
    "amount": 2800000.0,
    "status": "PENDING_VERIFICATION",
    "senderPhone": "09683776164",
    "screenshotUrl": "https://pub-11021a51faf24764b674a6afdc69061c.r2.dev/receipts/535734b5-50f8-4696-9422-e43ee93a1647_kpay.jpg",
    "aiVerificationNotes": "System failed to read the receipt image automatically. Manual review required.",
    "updatedAt": "2026-08-18T10:32:26.213775"
}
```

---

**Endpoint 2:** View Seller's Incoming Orders

URL: `/api/transactions/seller/{sellerId}`

Method: `GET`

Access: `Seller Role (ROLE_SELLER)`


#### response
```json
[
    {
        "id": 1,
        "productId": 1,
        "productTitle": "Yamaha Sniper 150 - Used",
        "amount": 2800000.0,
        "status": "PENDING_VERIFICATION",
        "senderPhone": "09683776164",
        "screenshotUrl": "https://pub-11021a51faf24764b674a6afdc69061c.r2.dev/receipts/535734b5-50f8-4696-9422-e43ee93a1647_kpay.jpg",
        "aiVerificationNotes": "System failed to read the receipt image automatically. Manual review required.",
        "updatedAt": "2026-08-18T10:32:26.213775"
    }
]
```

---
## Important

**Endpoint 3:** Manual Seller Verification & Escrow Lock

URL: `/api/transactions/{transactionId}/approve/{sellerId}`

Method: `POST`

Access: `Seller Role (ROLE_SELLER)`

#### Sample Response (200 OK) — After Seller Approves

```json
{
    "id": 1,
    "productId": 1,
    "productTitle": "Yamaha Sniper 150 - Used",
    "amount": 2800000.0,
    "status": "ESCROW_LOCKED",
    "senderPhone": "09683776164",
    "screenshotUrl": "https://pub-11021a51faf24764b674a6afdc69061c.r2.dev/receipts/535734b5-50f8-4696-9422-e43ee93a1647_kpay.jpg",
    "aiVerificationNotes": "Manually verified and accepted by seller.",
    "updatedAt": "2026-08-18T10:45:10.123456"
}
```

---

**Endpoint 4:** Generate QR Code for Handover

`URL: /api/transactions/{transactionId}/qr`

Method: `GET`

Access: `Seller Role (ROLE_SELLER)` and the seller must own the transaction.

Returns: `image/png`. The QR image encodes the transaction ID and its one-time token. It is available only while the transaction status is `ESCROW_LOCKED`.

---

**Endpoint 5:** Complete Handover with a Scanned QR Token

`URL: /api/transactions/release`

Method: `POST`

Access: the transaction's buyer or seller.

```json
{
  "transactionId": 1,
  "qrToken": "scanned-one-time-token"
}
```

A valid token changes the status from `ESCROW_LOCKED` to `COMPLETED` and logs payout intent for the seller.

---

**Endpoint 6:** Cancel and Refund Before Handover

`URL: /api/transactions/cancel`

Method: `POST`

Access: the buyer who created the transaction.

```json
{
  "transactionId": 1,
  "reason": "Item was not as described"
}
```

Pending or escrow-locked transactions are changed to `CANCELLED_AND_REFUNDED`. Completed transactions cannot be cancelled.


