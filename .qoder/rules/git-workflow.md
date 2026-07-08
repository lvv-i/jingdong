# Rule: Git Workflow

## Scope

Applies to all source code repositories used by the e-commerce project team.

## Branch Model

```
main (production-ready demo)
  ↑
develop (daily integration)
  ↑
feature/xxx
  ↑
fix/xxx
```

### Branches

| Branch | Purpose | Protection |
|--------|---------|------------|
| `main` | Stable demo version | Force push forbidden; only merge from `develop` via PR |
| `develop` | Daily development integration | Force push forbidden; merge via PR |
| `feature/<module>-<desc>` | New feature development | Delete after merge |
| `fix/<desc>` | Bug fix | Delete after merge |

### Branch Naming Examples

- `feature/user-order-list`
- `feature/merchant-product-crud`
- `feature/admin-audit-log`
- `fix/order-status-transition`
- `fix/login-token-expiry`

## Commit Message Convention

Use the format:

```
type(scope): subject
```

Examples:

- `feat(order): 添加订单列表分页查询`
- `fix(product): 修复商品下架后仍可见的问题`
- `docs(api): 更新订单接口文档`
- `style(admin): 调整后台表格列宽`
- `refactor(user): 提取用户认证公共方法`
- `test(refund): 添加退款流程单元测试`

### Types

| Type | Meaning |
|------|---------|
| feat | New feature |
| fix | Bug fix |
| docs | Documentation only |
| style | Formatting, no logic change |
| refactor | Code restructuring |
| test | Adding or updating tests |
| chore | Build, config, dependencies |

## Pull Request Rules

1. Every merge to `develop` or `main` must go through a Pull Request.
2. PR title must follow commit message convention.
3. PR description must include:
   - What changed
   - Which modules are affected
   - How to test
   - Screenshots if UI changed
4. At least one reviewer must approve before merging.
5. Resolve all review comments before merging.
6. Delete feature/fix branches after merging.

## Code Review Checklist

Reviewer must verify:

- [ ] Code follows team style rules.
- [ ] API response format matches the contract.
- [ ] Database changes follow conventions.
- [ ] State transitions are legal.
- [ ] No hardcoded secrets or credentials.
- [ ] Frontend does not rely solely on UI for access control.
- [ ] Tests or demo steps are included.

## Conflict Resolution

- Rebase feature branch onto latest `develop` before creating PR if possible.
- If conflicts are complex, merge `develop` into feature branch and resolve locally.
- Never force push to `main` or `develop`.

## Demo Day Preparation

- One day before demo, freeze `main` branch.
- Only critical bug fixes via `fix/` branches merged to `main` with team lead approval.
- Tag the demo version: `git tag demo-v1.0`.

## Forbidden Practices

- No direct pushes to `main` or `develop`.
- No meaningless commit messages like `update`, `fix bug`, `123`.
- No long-lived feature branches without regular rebasing.
- No committing binary build outputs or `node_modules`.
