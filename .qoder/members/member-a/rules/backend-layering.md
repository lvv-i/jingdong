# Rule (A专属): 后端分层与包结构规范

## Scope

适用于成员 A 编写的全部后端代码（第一阶段文档中提到的工程结构，第二阶段落地实现）。在团队 code-style.md 之上的后端细化规范。

## 单体工程包结构

```
com.example.shop
├── common/                 # 公共：ApiResult、BusinessException、GlobalExceptionHandler、常量
├── config/                 # 配置：WebConfig、SecurityConfig(拦截器注册)、MybatisConfig
├── security/               # 鉴权：JwtUtil、AuthInterceptor、UserContext(当前登录人)
├── controller/             # 用户端接口 /api/**
├── merchant/controller/    # 商家端接口 /api/merchant/**
├── admin/controller/       # 管理员端接口 /api/admin/**
├── service/                # 业务接口
├── service/impl/           # 业务实现（事务边界在这里）
├── mapper/                 # MyBatis Mapper 接口（只访问数据库）
├── entity/                 # 与表一一对应的实体
├── dto/                    # 请求体对象（含校验注解）
├── vo/                     # 响应视图对象
└── enums/                  # 状态枚举（OrderStatus/RefundStatus/AuditStatus/ProductStatus）
```

## 分层职责（不可越层）

| 层 | 职责 | 禁止 |
|----|------|------|
| Controller | HTTP 映射、参数接收、调 Service、包装 ApiResult | 写业务逻辑、直接调 Mapper |
| Service/Impl | 业务规则、状态流转校验、@Transactional | 直接操作 Request/Response |
| Mapper | 单表/关联 SQL 访问 | 写业务判断 |
| Entity | 与表字段对应（camelCase） | 携带请求/响应语义 |

## 关键约定

1. **状态枚举**：`enums/` 下定义 OrderStatus、RefundStatus、AuditStatus、ProductStatus，值必须与第一阶段 T1 状态机枚举表一致；状态流转校验集中在 Service 层。
2. **统一返回**：所有 Controller 返回 `ApiResult<T>`（code/message/data/total），禁止直接返回裸对象。
3. **异常**：业务错误抛 `BusinessException(code, message)`（code 来自 T3 错误码分段表），由 GlobalExceptionHandler 统一转 ApiResult；禁止向调用方暴露堆栈。
4. **事务**：订单创建（主表+明细+库存扣减）、支付、退款等跨表操作必须 @Transactional；状态变更同时写 audit_logs。
5. **SQL**：写在 Mapper XML，禁止在 Java 代码拼接 SQL；所有查询带 `deleted_flag = 0`（除管理员恢复场景）。
6. **命名**：类 UpperCamelCase、方法 lowerCamelCase、常量 UPPER_SNAKE_CASE；Controller 方法名与 T5 接口清单的编号可对应注释。
