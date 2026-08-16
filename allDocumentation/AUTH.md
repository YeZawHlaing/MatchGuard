## Authentication & Authorization Documentation

This document serves as the official guide for teammates implementing or consuming authentication and role-based authorization in the **MatchGuard** backend.

MatchGuard uses **Stateless JWT (JSON Web Tokens)** containing an Access Token (short-lived) and a Refresh Token (long-lived), coupled with Role-Based Access Control (RBAC) for `CUSTOMER` and `SELLER` roles.

---
### Base URL

```http request
 http://localhost:8080
```

### Authentication Endpoints
1. **Register a New User**

- **Endpoint:**
```http request
POST /api/auth/register
```
- **Access:** Public (No authentication required)
- **Description:** Creates a new account with a specified role `(CUSTOMER or SELLER)` and issues initial tokens.

### Sample Json Request
```json
{
  "name": "XoXo's store",
  "email": "xoxo@toner.gmail.com",
  "phone": "0968444356",
  "password": "xoxo123",
  "role": "SELLER"
}
```

### Sample Json Response
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4b3hvQHRvbmVyLmdtYWlsLmNvbSIsImlhdCI6MTc4Njg4NzA1MSwiZXhwIjoxNzg2OTczNDUxfQ.uZPIi1L-FYZvzTOVUm-RH9CLw8AD0sIsgeW7le_kdYI",
    "refreshToken": "294df986-5006-4d94-bbbc-0a5160cc6b6d",
    "tokenType": null,
    "id": 2,
    "name": "XoXo's store",
    "email": "xoxo@toner.gmail.com",
    "role": "SELLER"
}
```

---

2. **User Login**

- **Endpoint:** 
```http request
POST /api/auth/login
```

- **Access:** Public

- **Description:** Authenticates user credentials via email and password, returning fresh tokens.

### Sample Json Request
```json
{
    "email": "xoxo@toner.gmail.com",
    "password": "xoxo123"
}
```

### Sample Json Response
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4b3hvQHRvbmVyLmdtYWlsLmNvbSIsImlhdCI6MTc4Njg4NzA3NCwiZXhwIjoxNzg2OTczNDc0fQ.bE5NmIobPJ9rI1tn0m1YZZ9zrmyJpdYGPUvo_nDOunA",
    "refreshToken": "30c0fb6b-f7cc-47c6-ab01-1947bcc4642c",
    "tokenType": null,
    "id": 2,
    "name": "XoXo's store",
    "email": "xoxo@toner.gmail.com",
    "role": "SELLER"
}
```



