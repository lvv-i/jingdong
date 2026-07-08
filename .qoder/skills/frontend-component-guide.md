# Skill: Frontend Component Guide

## Description

Guide frontend developers to choose the right components and patterns for the user web app, admin dashboard, and mobile app. Ensure form validation, list pagination, status display, and API calls follow the team conventions.

## When to Use

- Building a new page in the user web app, admin dashboard, or uni-app mobile app.
- Choosing between Element Plus and uni-app components.
- Standardizing list pages, forms, and detail pages.
- Reviewing frontend code for consistency.

## Tech Stacks

| Target | Framework | UI Library |
|--------|-----------|------------|
| User web app | Vue 3 + Vite | Element Plus |
| Admin dashboard | Vue 3 + Vite | Element Plus |
| Mobile app | uni-app | uni-app built-in components + uView (optional) |

## Common Page Patterns

### 1. List Page with Pagination

Use `el-table` + `el-pagination` for Element Plus; use `scroll-view` or custom list with pagination for uni-app.

Element Plus example:

```vue
<template>
  <div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag>{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @change="fetchList"
    />
  </div>
</template>
```

### 2. Form with Validation

Use `el-form` + `el-form-item` with validation rules.

```vue
<el-form :model="form" :rules="rules" ref="formRef">
  <el-form-item label="商品标题" prop="title">
    <el-input v-model="form.title" />
  </el-form-item>
  <el-form-item label="价格" prop="price">
    <el-input-number v-model="form.price" :min="0" :precision="2" />
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="submit">提交</el-button>
  </el-form-item>
</el-form>
```

Rules:

```js
const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
};
```

### 3. Status Display

All status values come from backend enums. Frontend must map them to labels and tags, never invent new states.

```js
const ORDER_STATUS_MAP = {
  PENDING_PAY: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付待发货', type: 'primary' },
  SHIPPED: { label: '已发货待收货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
};
```

### 4. API Call Pattern

Use a shared request utility that adds JWT token and handles the unified response.

```js
import request from '@/utils/request';

export function getOrderList(params) {
  return request.get('/api/orders', { params });
}
```

Handle `code !== 200` with `ElMessage.error(message)`.

## Mobile (uni-app) Specifics

- Use `<view>`, `<text>`, `<image>`, `<scroll-view>` as base components.
- Use `uni.request` or a shared wrapper for API calls.
- Use `uni.navigateTo` for page navigation.
- Keep pages lightweight; share logic with user web app through extracted composables when possible.
- Status bar and safe-area adaptation are required for App builds.

## Component Selection Reference

| Scenario | Element Plus | uni-app |
|----------|-------------|---------|
| Button | `el-button` | `<button>` / `uni-goods-nav` |
| Form input | `el-input` | `<input>` |
| Table | `el-table` | Custom `<view>` list |
| Pagination | `el-pagination` | Load more / page buttons |
| Dialog | `el-dialog` | `uni.showModal` |
| Toast | `ElMessage` | `uni.showToast` |
| Tag | `el-tag` | Custom styled text |

## Output Format

When asked for a frontend pattern:

1. Identify the target stack (Element Plus or uni-app).
2. Provide a minimal working code snippet.
3. Mention required imports and API conventions.
4. Note any role-based differences (user vs merchant vs admin).
