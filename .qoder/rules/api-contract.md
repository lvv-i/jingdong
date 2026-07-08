# Rule: API Contract

## Scope

Applies to all RESTful API endpoints exposed by the Spring Boot backend and consumed by the user web app, admin dashboard, and mobile app.

## Base Path

All endpoints are prefixed with `/api`.

Role prefixes:

| Role | Prefix | Example |
|------|--------|---------|
| Public / User | `/api/` | `/api/products` |
| Merchant | `/api/merchant/` | `/api/merchant/orders` |
| Admin | `/api/admin/` | `/api/admin/merchants` |

## Unified Response Format

Every response must use this envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "total": 0
}
```

Rules:

- `code` = 200 means success. HTTP status remains 200 unless a transport error occurs.
- `message` is a human-readable Chinese string.
- `data` is the payload. For list queries it must be `{ "list": [], "total": 0 }`.
- `total` mirrors the list total for paginated APIs; otherwise 0.

## Pagination Contract

List endpoints accept:

```
?page=1&pageSize=10
```

Defaults:

- `page` = 1
- `pageSize` = 10
- Maximum `pageSize` = 100

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

## Error Code Segments

| Segment | Range | Owner |
|---------|-------|-------|
| Common | 1000-1099 | Parameter, auth, system |
| User | 2000-2099 | User, address, profile |
| Product | 3000-3099 | Product, category, search |
| Order | 4000-4099 | Cart, order, payment simulation |
| Refund | 5000-5099 | Refund, after-sales |
| Merchant | 6000-6099 | Shop, merchant backend |
| Admin | 7000-7099 | Audit, governance, statistics |

Common codes:

- 1001 Illegal parameter
- 1002 Unauthorized
- 1003 Forbidden
- 1004 Resource not found
- 1005 System error

## Authentication

- Token is sent in the `Authorization: Bearer <token>` header.
- Token expiration is 24 hours for this course project.
- Refresh token is optional; if implemented, keep it separate.

## Authorization

- Permission checks must occur on the backend.
- Frontend may hide buttons, but the backend must reject unauthorized requests.
- Merchant endpoints must verify the resource belongs to the current merchant.
- Admin endpoints must verify the user has admin role.

## Validation

- Use Jakarta Bean Validation (`@NotNull`, `@Size`, `@Min`, `@Max`) on DTOs.
- Return code 1001 with a clear Chinese message for validation failures.
- Never expose internal stack traces or database details in error messages.

## Idempotency

- Payment simulation and order creation should include idempotency keys when possible.
- For this course project, at minimum avoid duplicate submissions via frontend debounce and backend unique constraints.

## Logging

- Log every request with trace ID, user ID, endpoint, and response code.
- Log sensitive operations: order state change, refund decision, merchant audit.

## Forbidden Practices

- Do not return different response shapes for success and error.
- Do not use HTTP status codes as business codes.
- Do not rely solely on frontend for access control.
- Do not expose raw SQL or exception stack traces.
