## 系统模块
~~~
src
├── main
│   ├── java
│   │   └── com.mxh.bank
│   │       ├── config         # 配置类存放目录，如Redis、数据库等配置
│   │       ├── controller     # 控制器层，处理接口请求与响应
│   │       ├── exception      # 自定义异常类及异常处理相关
│   │       ├── model          # 实体类，映射数据库表或业务模型
│   │       ├── repository     # 数据访问层，通常用于操作数据库
│   │       ├── service        # 业务逻辑层，实现具体业务功能
│   │       ├── utils          # 工具类，通用方法、工具封装
│   │       └── AccountManagerServiceApplication  # 应用启动类
│   └── resources              # 资源文件目录，如配置文件、静态资源等
~~~
## 构建启动
#### 依次执行启动服务
- docker build -t account-manager:1.0 .
- docker-compose up -d

## 🏦 银行账户管理系统 API 文档

本接口文档描述了银行账户管理系统的 RESTful API，支持账户创建、查询、更新、删除、转账及分页查询功能。

---

### 📚 API 接口列表

| 端点 | 方法 | 描述 | 请求体 | 成功响应 |
|------|------|------|--------|----------|
| `/bank-manager/account` | `POST` | 创建新银行账户 | ✅ `CreateAccountRequestParam` | `AccountInfoResponse` |
| `/bank-manager/account/{account_number}` | `GET` | 根据账号查询账户信息 | ❌ | `AccountInfoResponse` |
| `/bank-manager/account/{account_number}` | `PUT` | 更新指定账号的账户信息 | ✅ `UpdateAccountRequestParam` | `AccountInfoResponse` |
| `/bank-manager/account/{account_number}` | `DELETE` | 删除指定账号的账户 | ❌ | `OperationResponse` |
| `/bank-manager/accounts` | `GET` | 分页查询所有账户（默认按 ID 降序） | ❌（支持分页参数） | `Page<AccountInfoResponse>` |
| `/bank-manager/account/transfer` | `POST` | 账户间转账 | ✅ `TransferRequestParam` | `OperationResponse` |

---

### 📝 请求参数说明

#### 分页参数（可选）
- 默认分页：`page=0`, `size=10`, 排序字段 `id`，方向 `DESC`
- 可通过查询参数自定义：`?page=0&size=20&sort=id,asc`

---

### 📤 响应结构说明

#### `AccountInfoResponse`
```json
{
  "accountNumber": "ACC123456",
  "ownerName": "张三",
  "balance": 1000.00,
  "currency": "CNY",
  "status": "ACTIVE",
  "createdAt": "2025-01-01T10:00:00"
}

