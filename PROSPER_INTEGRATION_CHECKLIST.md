# Prosper Backend Integration Checklist

Please provide the following details so I can configure Retrofit and port features to Cashwind Android.

## 1. API Base URL & Environment

- [ ] **Base URL**: `https://api.example.com/` or environment-specific endpoints (dev/staging/prod)
- [ ] **Protocol**: HTTP/HTTPS (port, if non-standard)
- [ ] **API version** (if any): e.g., `/v1`, `/v2`

## 2. Authentication

- [ ] **Auth method**: 
  - [ ] JWT (Bearer token)?
  - [ ] OAuth 2.0?
  - [ ] API Key (header, query param)?
  - [ ] Basic Auth (username/password)?
  - [ ] Custom?
- [ ] **Login endpoint**: `POST /login` or other?
- [ ] **Token storage**: How should tokens be persisted on-device?
- [ ] **Refresh mechanism**: Auto-refresh, manual, or header-based?

## 3. Core Features to Port (in priority order)

List the main flows/screens from Prosper you want in Cashwind:

1. [ ] **Example 1**: Login / Authentication
2. [ ] **Example 2**: Dashboard / Home
3. [ ] **Example 3**: Transactions / History
4. [ ] **Example 4**: User Profile
5. [ ] **Example 5**: Other critical feature

## 4. API Endpoints & Data Models

For each feature above, provide:

- **Endpoint path** (e.g., `GET /transactions`)
- **HTTP method** (GET, POST, PUT, DELETE)
- **Request body** (if applicable) — JSON example or schema
- **Response body** — JSON example or schema (field names, types, structure)
- **Authentication required?** (yes/no)
- **Query parameters** (pagination, filters, sorting)
- **Expected status codes** (200, 201, 400, 401, 404, 500, etc.)

### Example format

```text
GET /user/profile
- Auth: Required (Bearer token in Authorization header)
- Response:
  {
    "id": "user_123",
    "name": "John Doe",
    "email": "john@example.com",
    "balance": 1500.50,
    "createdAt": "2025-01-01T00:00:00Z"
  }
```

## 5. Special Requirements

- [ ] **Headers**: Any custom headers needed (e.g., `X-API-Key`, `User-Agent`)?
- [ ] **Timeouts**: Connection/read/write timeouts?
- [ ] **Rate limiting**: Requests per minute/hour?
- [ ] **Pagination**: How are lists paginated (limit/offset, cursor, page number)?
- [ ] **Error handling**: Error response format (e.g., `{ "error": "message" }` or `{ "errors": [...] }`)?
- [ ] **CORS / SSL pinning**: Any certificate pinning needed?

## 6. Design & Branding

- [ ] **Logo/icon**: PNG/SVG for app icon and splash
- [ ] **Color scheme**: Primary, secondary, accent colors (hex or Material color names)
- [ ] **Fonts**: Any custom fonts, or use Material Defaults?
- [ ] **UI style**: Material Design 3, custom, other?

## 7. Backend Repo / Documentation

- [ ] **Prosper repo/docs URL**: Can you share the Prosper repo or API docs link?
- [ ] **OpenAPI/Swagger spec**: If available?
- [ ] **Postman collection**: For testing endpoints?

---

## How to Share

1. **Fill this checklist** and reply with your answers.
2. **Attach JSON examples** or API docs if you have them.
3. **Share the Prosper repo** or point me to the backend code.

Once I have this, I will:
- Configure Retrofit with auth (token interceptor, login flow)
- Define Moshi data models for each endpoint
- Implement the first feature screen (e.g., Login) with real API calls
- Test with your backend
- Port additional features incrementally
