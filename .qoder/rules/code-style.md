# Rule: Code Style

## Scope

Applies to all Java backend code, Vue frontend code, and SQL scripts written for the JD-style e-commerce platform.

## Java Conventions

### Naming

- Package names: lowercase, e.g., `com.example.shop.controller`.
- Class names: UpperCamelCase, e.g., `OrderServiceImpl`.
- Method and variable names: lowerCamelCase, e.g., `getOrderById`.
- Constants: UPPER_SNAKE_CASE, e.g., `MAX_PAGE_SIZE`.
- Entity fields match database column names in camelCase.

### Layering

- Controller only handles HTTP mapping and response wrapping.
- Service contains business logic and transaction boundaries.
- Mapper only accesses the database.
- Do not write SQL in controllers or business logic in mappers.

### Comments

- Every public class and method must have a JavaDoc or single-line comment in Chinese.
- Complex business logic must include inline comments explaining "why", not "what".

### Exception Handling

- Use custom `BusinessException` for business errors; never return raw exception messages to frontend.
- Controller layer uses a global exception handler (`@RestControllerAdvice`).
- Log unexpected exceptions with stack traces; log business exceptions at INFO/WARN level.

### Lombok

- Use `@Data`, `@RequiredArgsConstructor`, `@Slf4j` appropriately.
- Avoid `@Data` on entities if you need fine-grained control over equals/hashCode; prefer `@Getter` and `@Setter`.

## Vue / JavaScript Conventions

### Naming

- Components: PascalCase, e.g., `OrderList.vue`.
- Composables: camelCase starting with `use`, e.g., `useOrderList.js`.
- API functions: camelCase, e.g., `getOrderList`.
- Pinia stores: camelCase, e.g., `useUserStore`.

### File Organization

```
src
  api          # API request functions
  assets       # Static assets
  components   # Shared components
  views        # Page components
  stores       # Pinia stores
  utils        # Request wrapper, helpers
  router       # Route definitions
```

### Template Style

- Use 2 spaces for indentation.
- Always provide `:key` for `v-for`.
- Use `<script setup>` for Vue 3 components.
- Do not use `var`; use `let` and `const`.

## SQL Conventions

- Use lowercase for keywords (`select`, `from`, `where`).
- Indent subqueries and joins for readability.
- Always qualify column names in multi-table queries.
- Parameterize queries; never concatenate user input into SQL.

## Enforcement

When reviewing code:

1. Reject code that violates naming conventions.
2. Reject code that mixes layers.
3. Reject code that returns raw exception messages.
4. Reject SQL without comments on tables and columns.
