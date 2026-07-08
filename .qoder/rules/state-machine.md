# Rule: State Machine

## Scope

Applies to all order, refund, and merchant onboarding state transitions in the e-commerce platform.

## Principle

All state values are defined and owned by the backend. Frontend is read-only. No module may invent new state names or bypass allowed transitions.

## Order State Machine

### States

- `PENDING_PAY` - 待支付
- `PAID` - 已支付待发货
- `SHIPPED` - 已发货待收货
- `COMPLETED` - 已完成
- `CANCELLED` - 已取消

### Allowed Transitions

```
PENDING_PAY --用户支付--> PAID
PENDING_PAY --用户取消/超时--> CANCELLED
PAID --商家发货--> SHIPPED
SHIPPED --用户确认收货--> COMPLETED
```

### Rules

- Only unpaid orders (`PENDING_PAY`) can be cancelled.
- Payment is simulated; the backend must verify stock and price snapshot before changing state to `PAID`.
- Shipping requires a tracking number; the backend records it.
- Once `COMPLETED`, the order is closed and cannot be refunded through the order state.

## Refund State Machine

### States

- `REFUNDING` - 退款中
- `MERCHANT_AGREED` - 商家同意
- `MERCHANT_REJECTED` - 商家拒绝
- `ADMIN_INTERVENED` - 平台介入
- `REFUNDED` - 已退款
- `CLOSED` - 已关闭

### Allowed Transitions

```
REFUNDING --商家同意--> MERCHANT_AGREED --系统退款--> REFUNDED
REFUNDING --商家拒绝--> MERCHANT_REJECTED --用户申请平台介入--> ADMIN_INTERVENED --管理员裁决--> REFUNDED / CLOSED
REFUNDING --用户撤销--> CLOSED
```

### Rules

- A refund request must be linked to an order that is `PAID`, `SHIPPED`, or `COMPLETED`.
- The refund record must persist `merchant_reply` and `admin_result` for auditing.
- Final states are `REFUNDED` and `CLOSED`; no further transitions allowed.
- If order has already been fully refunded, no new refund request is allowed.

## Merchant Onboarding State Machine

### States

- `PENDING_AUDIT` - 待审核
- `APPROVED` - 已通过
- `REJECTED` - 已驳回

### Allowed Transitions

```
PENDING_AUDIT --管理员通过--> APPROVED
PENDING_AUDIT --管理员驳回--> REJECTED
```

### Rules

- Merchant can submit shop info only once per user until rejected.
- Admin approval/rejection must include a reason saved in `audit_reason`.
- Only approved merchants can publish products and process orders.

## Product State Machine

### States

- `DRAFT` - 草稿
- `PENDING_AUDIT` - 待审核（可选）
- `ON_SALE` - 已上架
- `OFF_SALE` - 已下架

### Allowed Transitions

```
DRAFT --商家上架--> ON_SALE
ON_SALE --商家下架--> OFF_SALE
OFF_SALE --商家上架--> ON_SALE
```

### Rules

- Only `ON_SALE` products are visible on user web/app/mini-program.
- Product state transitions do not affect historical order data.

## Implementation Rules

1. Backend must validate every state transition; reject illegal transitions with code 1001.
2. Use explicit service methods such as `payOrder`, `shipOrder`, `confirmReceipt`, `agreeRefund` instead of generic `updateStatus`.
3. State changes must update `updated_at`.
4. Sensitive state changes must write an `audit_log` record.

## Forbidden Practices

- Frontend must never change state directly.
- Do not allow transitions not listed above.
- Do not delete state history; keep records and logs.
- Do not use numeric state codes that differ between modules.
