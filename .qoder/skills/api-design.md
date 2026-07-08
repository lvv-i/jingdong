# Skill: API Design for JD-Style E-Commerce Platform

## Description

Guide team members to design RESTful API endpoints for the shared Spring Boot backend. Every API must follow the team contract: unified response envelope, fixed pagination shape, role-based access control, and clear error code segmentation.

## When to Use

- Adding a new business module (user, product, order, refund, merchant, admin).
- Reviewing an existing API design for consistency.
- Generating OpenAPI/Swagger annotations or API documentation.
- Writing frontend service layer code that calls the backend.

## Team API Contract

### 1. Base URL & Versioning

- All endpoints are prefixed with `/api`.
- No URL versioning for this course project; use module prefix instead: `/api/products`, `/api/orders`, `/api/merchant/orders`, `/api/admin/merchants`.

### 2. HTTP Methods

| Action | Method |
|--------|--------|
| Query / Get one | GET |
| Create | POST |
| Full update | PUT |
| Partial update | PATCH (optional) |
| Delete | DELETE |

### 3. Unified Response Format

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "total": 0
}
```

Rules:

- `code` is a business code. `200` means success. HTTP status code is always `200 OK` for business responses unless a real transport error occurs.
- `message` is human-readable. Use Chinese for team consistency.
- `data` holds the payload. For list queries it is an object: `{ "list": [], "total": 0 }`.
- `total` is only meaningful for paginated list APIs; otherwise `0` or omitted.

### 4. Pagination Contract

Request parameters:

```
GET /api/orders?page=1&pageSize=10&status=PAID
```

Response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100
  },
  "total": 100
}
```

- Default: `page = 1`, `pageSize = 10`, max `pageSize = 100`.
- Sorting: use `sortField` and `sortOrder` (asc/desc) when needed.

### 5. Error Code Segments

| Segment | Range | Module |
|---------|-------|--------|
| Common | 1000-1099 | Parameter, auth, system |
| User | 2000-2099 | User, address, profile |
| Product | 3000-3099 | Product, category, search |
| Order | 4000-4099 | Cart, order, payment simulation |
| Refund | 5000-5099 | Refund, after-sales |
| Merchant | 6000-6099 | Shop, merchant backend |
| Admin | 7000-7099 | Audit, governance, statistics |

Common examples:

- `1001` Illegal parameter
- `1002` Unauthorized
- `1003` Forbidden
- `1004` Resource not found
- `1005` System error

### 6. Role-Based URL Prefixes

| Role | URL Prefix | Example |
|------|-----------|---------|
| Public | `/api/` | `/api/products` |
| User | `/api/` with user token | `/api/orders` |
| Merchant | `/api/merchant/` | `/api/merchant/orders` |
| Admin | `/api/admin/` | `/api/admin/merchants` |

### 7. Security Rules

- Token is passed in `Authorization: Bearer <token>` header.
- Permission check must happen on the backend; frontend only hides buttons.
- Sensitive operations (order cancellation, refund, merchant audit) must log operator info.

## Design Workflow

When asked to design an API:

1. Identify the business module and role.
2. Choose HTTP method and URL path.
3. Define request parameters / body with types and validation rules.
4. Define success response using the unified envelope.
5. List possible error codes from the appropriate segment.
6. Add a one-sentence description of the access control rule.

## Example

**Create order**

- Method: POST
- URL: `/api/orders`
- Request body:

```json
{
  "addressId": 1,
  "cartItemIds": [10, 11],
  "remark": "请发顺丰"
}
```

- Success response:

```json
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "orderId": 10001,
    "orderNo": "JD202607080001",
    "status": "PENDING_PAY",
    "payAmount": 299.00,
    "createdAt": "2026-07-08T14:30:00"
  }
}
```

- Error codes: `1001` parameter error, `2004` address not found, `3005` stock insufficient, `4001` cart item invalid.
- Access control: login user only, cart items must belong to current user.

## Output Format

For each requested API, return:

1. Method + URL
2. Role / access control
3. Request (parameters or body)
4. Success response example
5. Error codes
6. Backend implementation hints (entity / service method names)
