# Cashwind Android Integration with Prosper Backend

## Backend Summary
**Prosper** is a full-stack financial management app (Express + Prisma backend, React frontend).

### Base URL & Environment

- **Default**: `http://127.0.0.1:4000` (local development)
- **Production**: To be configured (set in `RetrofitProvider.kt`)
- **Port**: `4000` (configurable via `PORT` env var)

### Authentication

- **Method**: JWT Bearer Token (7-day expiry)
- **Login Endpoint**: `POST /auth/register` and `POST /auth/login`
- **Response**: `{ "token": "...", "user": { "id", "email", "name" } }`
- **Token Usage**: `Authorization: Bearer <token>` header on all protected endpoints
- **Storage**: SharedPreferences (Android native or EncryptedSharedPreferences for security)

---

## API Endpoints for Cashwind

### Authentication Endpoints

#### Register

```http
POST /auth/register
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "password123",
  "name": "User Name"
}

Response (201):
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

#### Login

```http
POST /auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200):
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

---

### Bills (Core Feature)

#### List All Bills

```http
GET /bills
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "userId": 1,
    "name": "Electric Bill",
    "amount": 150.50,
    "dueDate": "2025-02-01",
    "isPaid": false,
    "category": "utilities",
    "recurring": false,
    "frequency": null,
    "notes": "Monthly utility",
    "webLink": null,
    "createdAt": "2025-01-01T10:00:00Z"
  }
]
```

#### Create Bill

```http
POST /bills
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "Internet Bill",
  "amount": 79.99,
  "dueDate": "2025-02-15",
  "category": "utilities",
  "recurring": true,
  "frequency": "monthly",
  "notes": "ISP monthly subscription",
  "webLink": "https://example.com/billing"
}

Response (201):
{ "id": 2, "userId": 1, ...same as list response }
```

#### Update Bill

```http
PUT /bills/:id
Authorization: Bearer <token>
Content-Type: application/json

Request (partial update allowed):
{
  "isPaid": true,
  "amount": 85.50
}

Response (200):
{ ...updated bill }
```

#### Delete Bill

```http
DELETE /bills/:id
Authorization: Bearer <token>

Response (204): No content
```

---

### Accounts

#### List All Accounts

```http
GET /accounts
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "userId": 1,
    "type": "checking",
    "name": "Main Checking",
    "balance": 5000.00,
    "accountType": "bank",
    "creditLimit": null,
    "interestRate": null,
    "minimumPayment": null,
    "dueDay": null,
    "createdAt": "2025-01-01T00:00:00Z"
  }
]
```

#### Create Account

```http
POST /accounts
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "type": "checking",
  "name": "Savings Account",
  "balance": 2000.00,
  "accountType": "bank"
}

Response (201):
{ "id": 2, "userId": 1, ...same as list }
```

#### Update Account

```http
PUT /accounts/:id
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "balance": 2500.00
}

Response (200):
{ ...updated account }
```

#### Delete Account

```http
DELETE /accounts/:id
Authorization: Bearer <token>

Response (204): No content
(Error 400 if account has related transactions/bills)
```

---

### Transactions

#### List All Transactions
```
GET /transactions
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "userId": 1,
    "amount": 500.00,
    "type": "income",
    "category": "salary",
    "description": "Monthly salary",
    "tags": ["paycheck"],
    "isRecurring": true,
    "frequency": "monthly",
    "date": "2025-01-15",
    "accountId": 1,
    "account": {
      "name": "Main Checking",
      "type": "checking"
    },
    "createdAt": "2025-01-15T09:00:00Z"
  }
]
```

#### Create Transaction
```
POST /transactions
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "amount": 45.50,
  "type": "expense",
  "category": "dining",
  "description": "Lunch",
  "accountId": 1,
  "date": "2025-01-20",
  "tags": ["restaurant"],
  "isRecurring": false
}

Response (201):
{ "id": 2, ...same as list }
```

#### Update Transaction
```
PUT /transactions/:id
Authorization: Bearer <token>
Content-Type: application/json

Request (partial):
{
  "category": "groceries"
}

Response (200):
{ ...updated transaction }
```

#### Delete Transaction
```
DELETE /transactions/:id
Authorization: Bearer <token>

Response (204): No content
```

#### Transaction Analytics
```
GET /transactions/analytics?period=2025-01
Authorization: Bearer <token>

Response (200):
{
  "income": 5000.00,
  "expenses": 1500.00,
  "byCategory": {
    "salary": 5000.00,
    "dining": 450.00,
    "groceries": 800.00,
    "utilities": 250.00
  }
}
```

---

### Budgets

#### List All Budgets
```
GET /budgets
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "userId": 1,
    "name": "Monthly Food",
    "amount": 600.00,
    "period": "2025-01",
    "category": "groceries",
    "createdAt": "2025-01-01T00:00:00Z"
  }
]
```

#### Create Budget
```
POST /budgets
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "January Dining",
  "amount": 200.00,
  "period": "2025-01",
  "category": "dining"
}

Response (201):
{ "id": 2, ...same as list }
```

#### Update Budget
```
PUT /budgets/:id
Authorization: Bearer <token>

Request:
{
  "amount": 250.00
}

Response (200):
{ ...updated budget }
```

#### Delete Budget
```
DELETE /budgets/:id
Authorization: Bearer <token>

Response (204): No content
```

#### Budget Progress Analytics
```
GET /budgets/progress
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "name": "Monthly Food",
    "amount": 600.00,
    "spent": 425.30,
    "remaining": 174.70,
    "percentUsed": 70.88
  }
]
```

---

### Goals

#### List All Goals
```
GET /goals?category=savings&sort=targetDate
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "userId": 1,
    "name": "Emergency Fund",
    "type": "savings",
    "targetAmount": 10000.00,
    "currentAmount": 2500.00,
    "targetDate": "2025-12-31",
    "monthlyContribution": 625.00,
    "priority": "high",
    "status": "active",
    "accountId": 1,
    "category": "savings",
    "notes": "6-month emergency fund",
    "account": {
      "name": "Savings Account",
      "type": "savings"
    }
  }
]
```

#### Create Goal
```
POST /goals
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "Vacation Fund",
  "type": "savings",
  "targetAmount": 5000.00,
  "currentAmount": 1000.00,
  "targetDate": "2025-08-31",
  "priority": "medium",
  "accountId": 2,
  "category": "travel",
  "notes": "Summer vacation"
}

Response (201):
{ "id": 2, ...same as list }
```

#### Goal Progress Analytics
```
GET /goals/progress
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currentAmount": 2500.00,
    "percentComplete": 25.00,
    "remainingAmount": 7500.00,
    "monthsRemaining": 12,
    "projectedCompletion": "2025-12-31"
  }
]
```

---

### Settings & Notifications

#### Get Notification Settings
```
GET /settings/notifications
Authorization: Bearer <token>

Response (200):
{
  "phone": "+1-555-123-4567",
  "emailNotifications": true,
  "smsNotifications": false,
  "reminderDays": 7
}
```

#### Update Notification Settings
```
PUT /settings/notifications
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "phone": "+1-555-987-6543",
  "emailNotifications": true,
  "smsNotifications": true,
  "reminderDays": 5
}

Response (200):
{ ...updated settings }
```

---

## Error Responses

All endpoints return error details on failure:

```json
{
  "error": "Descriptive error message"
}
```

### Common Status Codes
- **200 OK**: Success
- **201 Created**: Resource created
- **204 No Content**: Success (delete)
- **400 Bad Request**: Validation error
- **401 Unauthorized**: Missing/invalid token
- **403 Forbidden**: Access denied (user doesn't own resource)
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## Feature Priority for Android Port

1. **Authentication** (Login/Register)
2. **Dashboard** (Overview, summary cards)
3. **Bills Management** (List, Create, Update, Mark Paid, Delete)
4. **Accounts** (List, Create, View Balance)
5. **Transactions** (List, Create, Analytics)
6. **Budgets** (List, Create, Progress)
7. **Goals** (List, Create, Progress)
8. **Notifications & Settings**

---

## Integration Checklist for Cashwind Android

- [ ] Configure base URL in `RetrofitProvider.kt` (set to Prosper backend URL)
- [ ] Create DTO classes (Moshi models) for all API responses
- [ ] Add token interceptor for automatic Bearer token injection
- [ ] Implement login/register screens with Retrofit calls
- [ ] Store JWT token securely in EncryptedSharedPreferences
- [ ] Build dashboard screen (fetch overview data)
- [ ] Implement bills list + create/edit/delete screens
- [ ] Add accounts, transactions, budgets, and goals screens
- [ ] Handle API errors and network failures gracefully
- [ ] Add pull-to-refresh and pagination where needed
- [ ] Test on emulator/device against live backend

