# Skill: Test Case & Acceptance Checklist Generator

## Description

Generate test cases and acceptance checklists for the e-commerce platform. Focus on business flow completeness, role-based permission boundaries, and reproducible demo scenarios.

## When to Use

- Before a feature enters test phase.
- Preparing for course project demo.
- Writing test scripts for order, refund, merchant audit, and admin governance flows.
- Creating fixed demo data and test accounts.

## Fixed Test Accounts

The team should prepare these accounts at the beginning of development and keep them stable:

| Role | Username | Password | Purpose |
|------|----------|----------|---------|
| 普通用户 | user001 | 123456 | 浏览、下单、申请退款 |
| 商家 | merchant001 | 123456 | 发布商品、发货、处理退款 |
| 管理员 | admin001 | 123456 | 审核商家、介入售后、查看统计 |

## Core Business Flows

### Flow 1: User Purchase

1. User logs in.
2. Search / browse products.
3. Add product to cart.
4. Go to settlement page.
5. Submit order -> status becomes `PENDING_PAY`.
6. Click simulated payment -> status becomes `PAID`.
7. Merchant ships -> status becomes `SHIPPED`.
8. User confirms receipt -> status becomes `COMPLETED`.

### Flow 2: User Refund

1. User goes to order center.
2. Selects a paid/shipped order and applies for refund.
3. Refund status becomes `REFUNDING`.
4. Merchant agrees -> status becomes `MERCHANT_AGREED` -> system refunds -> status becomes `REFUNDED`.
5. Alternative: merchant rejects -> status becomes `MERCHANT_REJECTED` -> user requests platform intervention -> admin handles -> final status `REFUNDED` or `CLOSED`.

### Flow 3: Merchant Onboarding

1. Merchant registers and submits shop info.
2. Shop status becomes `PENDING_AUDIT`.
3. Admin approves -> status becomes `APPROVED`.
4. Alternative: admin rejects -> status becomes `REJECTED` with reason.

### Flow 4: Product Lifecycle

1. Merchant creates product -> status `DRAFT`.
2. Merchant puts product on shelf -> status `ON_SALE`.
3. User can see product on user web/app/mini-program.
4. Merchant takes product off shelf -> status `OFF_SALE`.
5. Product is no longer visible to users.

## Test Case Template

Each test case should include:

| Field | Description |
|-------|-------------|
| ID | TC-模块-序号，如 TC-ORDER-001 |
| Title | 一句话描述测试目的 |
| Preconditions | 必须满足的初始状态 |
| Steps | 操作步骤 |
| Expected Result | 期望结果 |
| Priority | P0 / P1 |
| Roles | 涉及角色 |

## Example Test Case

**TC-ORDER-001: 用户成功下单并模拟支付**

- Preconditions: 用户已登录，商品库存充足，商品状态为 `ON_SALE`。
- Steps:
  1. 用户浏览商品并加入购物车。
  2. 进入结算页选择收货地址。
  3. 提交订单。
  4. 点击模拟支付按钮。
- Expected Result:
  - 提交订单后订单状态为 `PENDING_PAY`。
  - 模拟支付后状态变为 `PAID`。
  - 订单明细中价格、标题与下单时刻一致，不受后续商品修改影响。
- Priority: P0
- Roles: 普通用户

## Demo Data Checklist

Before each demo, verify:

- [ ] 测试账号可正常登录五个端。
- [ ] 固定商品信息（标题、价格、库存）未变。
- [ ] 存在一条待支付订单、一条已发货订单、一条已完成订单。
- [ ] 存在一条退款中/已退款售后单。
- [ ] 商家入驻审核记录完整。
- [ ] 启动脚本和数据库初始化脚本可一键运行。

## Output Format

When asked to generate tests:

1. Identify the business flow.
2. List fixed test accounts and preconditions.
3. Generate numbered steps and expected results.
4. Assign priority P0/P1.
5. Add demo data requirements if applicable.
