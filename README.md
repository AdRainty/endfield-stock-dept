# 终末地调度券交易模拟系统

> **ENDFIELD TRADING SYSTEM** - 工业科幻风格交易终端

基于《明日方舟：终末地》游戏设定创建的调度券交易模拟系统。

> 本项目大部分功能由AI Coding实现，可能存在部分Bug

---

## 设计特色

### 工业科幻终端风格

```
┌─────────────────────────────────────────────────────────────┐
│  ENDFIELD TRADING SYSTEM  ◆  ONLINE                        │
├─────────────────────────────────────────────────────────────┤
│  [行情中心]  [交易委托]  [持仓查询]  [管理后台]            │
└─────────────────────────────────────────────────────────────┘
```

### 视觉设计

| 元素 | 描述 |
|------|------|
| **主色调** | 霓虹橙 `#ff6b35` + 战术青 `#00d4c7` |
| **涨跌色** | 涨 `#ff4757` / 跌 `#2ed573` |
| **字体** | JetBrains Mono (等宽终端字体) |
| **特效** | CRT 扫描线、脉冲发光、故障艺术 |

---

## 技术栈

### 后端
| 组件 | 技术 |
|------|------|
| **框架** | Spring Boot 3.2.0 |
| **JDK** | 17 |
| **权限** | Sa-Token + Redis |
| **数据库** | PostgreSQL |
| **ORM** | MyBatis Plus |
| **数据库迁移** | Liquibase |
| **缓存** | Redis + Redisson |
| **API 文档** | Knife4j (Swagger) |
| **WebSocket** | Spring WebSocket |
| **HTTP 客户端** | Apache HttpClient 5 |

### 前端
| 组件 | 技术 |
|------|------|
| **框架** | React 18 |
| **构建工具** | Vite 5 |
| **UI 库** | Ant Design 5 |
| **图表** | Recharts |
| **HTTP** | Axios |
| **路由** | React Router 6 |

---

## 功能特性

### 交易所
- **四号谷底交易所**: 主要交易能源类调度券
- **武陵交易所**: 主要交易技术类调度券

### 交易品种
每个交易所有 4 个品种：
- 能源调度券
- 材料调度券
- 数据调度券
- 技术调度券

### 核心功能
1. **行情中心**: 实时价格、买卖五档、涨跌幅、K 线图
2. **交易委托**: 买入/卖出、价格/数量输入、委托记录、撤单
3. **持仓查询**: 持仓数量、成本价、盈亏计算、资金统计
4. **用户管理**: 用户列表、状态管理、原能分配

### 权限系统
- **普通用户**: 持仓查看、交易委托、行情查看
- **管理员**: 用户管理、原能分配、数据统计

### 登录方式
- **微信二维码登录**: 对接微信开放平台 OAuth2.0
- **快捷登录**: 测试环境快速接入

---

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- PostgreSQL 12+
- Redis 6+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE endfield WITH OWNER = endfield ENCODING = 'UTF8';
```

2. 启动应用后 Liquibase 会自动执行数据库迁移

### 环境变量配置

创建 `.env` 文件或配置系统环境变量：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=endfield
DB_USERNAME=endfield
DB_PASSWORD=endfield

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 微信开放平台配置（生产环境）
WECHAT_APP_ID=wx1234567890
WECHAT_APP_SECRET=your_app_secret
WECHAT_REDIRECT_URI=http://your-domain.com/api/auth/wx-callback
```

### 后端启动

```bash
cd stock-backend

# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

启动后访问：
- API 地址：http://localhost:8081/api
- API 文档：http://localhost:8081/api/doc.html

### 前端启动

```bash
cd stock-frontend

# 安装依赖
npm install

# 启动开发服务器
npm start
```

启动后访问：http://localhost:3000

---

## 项目结构

```
endfield-stock-dept/
├── stock-backend/              # Spring Boot 后端
│   ├── src/main/java/.../stock/
│   │   ├── config/             # 配置类
│   │   │   ├── RedisConfig.java       # Redis/Redisson 配置
│   │   │   ├── SaTokenConfig.java     # Sa-Token 配置
│   │   │   ├── WebSocketConfig.java   # WebSocket 配置
│   │   │   └── Knife4jConfig.java     # API 文档配置
│   │   ├── controller/         # 控制器
│   │   │   ├── AuthController.java    # 认证接口
│   │   │   ├── MarketController.java  # 行情接口
│   │   │   ├── TradeController.java   # 交易接口
│   │   │   ├── ExchangeController.java# 交易所接口
│   │   │   └── AdminController.java   # 管理接口
│   │   ├── service/            # 服务层
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── enums/              # 枚举
│   │   ├── exception/          # 异常处理
│   │   └── util/               # 工具类
│   │       └── WechatUtil.java        # 微信 API 工具
│   ├── src/main/resources/
│   │   ├── db/changelog/       # Liquibase 迁移脚本
│   │   │   ├── db.changelog-master.yaml
│   │   │   ├── 1.0.0-initial-schema.yaml
│   │   │   ├── 1.0.1-indexes.yaml
│   │   │   └── 1.0.2-initial-data.yaml
│   │   ├── application.yml     # 公共配置
│   │   ├── application-dev.yml # 开发环境配置
│   │   └── application-prod.yml# 生产环境配置
│   └── pom.xml
│
├── stock-frontend/             # React 前端
│   ├── src/
│   │   ├── pages/              # 页面组件
│   │   │   ├── Login.jsx       # 登录页
│   │   │   ├── Market.jsx      # 行情中心
│   │   │   ├── Trade.jsx       # 交易委托
│   │   │   ├── Position.jsx    # 持仓查询
│   │   │   └── Admin.jsx       # 管理后台
│   │   ├── layouts/            # 布局组件
│   │   ├── components/         # 公共组件
│   │   ├── services/           # API 服务
│   │   ├── index.css           # 全局样式
│   │   ├── App.jsx             # 应用入口
│   │   └── main.jsx            # 入口文件
│   └── package.json
│
├── .gitignore                  # Git 忽略文件
├── README.md                   # 项目文档
├── MIGRATION.md                # 数据库迁移文档
└── task.md                     # 开发任务列表
```

---

## API 接口

### 认证接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/wx-qrcode` | POST | 获取微信二维码 |
| `/api/auth/wx-qrcode/{scene}` | GET | 检查二维码状态 |
| `/api/auth/wx-callback` | GET | 微信回调接口 |
| `/api/auth/wx-login` | POST | 微信 code 登录 |
| `/api/auth/logout` | POST | 退出登录 |
| `/api/auth/user-info` | GET | 获取用户信息 |

### 交易所接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/exchange/list` | GET | 获取所有交易所 |
| `/api/exchange/{exchangeId}/instruments` | GET | 获取交易所品种 |

### 行情接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/market/instruments` | GET | 获取所有品种行情 |
| `/api/market/instruments/{exchangeId}` | GET | 获取交易所行情 |
| `/api/market/instrument/{instrumentCode}` | GET | 获取品种详情 |
| `/api/market/orderbook/{exchangeId}/{instrumentCode}` | GET | 获取买卖档口 |

### 交易接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/trade/account/{exchangeId}` | GET | 获取资金账户 |
| `/api/trade/position/{exchangeId}` | GET | 获取持仓列表 |
| `/api/trade/order` | POST | 下单委托 |
| `/api/trade/order/{orderNo}/cancel` | POST | 撤单 |
| `/api/trade/orders` | GET | 获取订单列表 |

### 管理员接口
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/allocate` | POST | 分配原能 |
| `/api/admin/users` | GET | 获取用户列表 |
| `/api/admin/user/{userId}` | GET | 获取用户详情 |
| `/api/admin/user/{userId}/status` | POST | 更新用户状态 |
| `/api/admin/allocations` | GET | 获取分配记录 |
| `/api/admin/statistics` | GET | 获取统计数据 |

---

## 游戏规则

### 初始资金
新用户注册后，每个交易所赠送 **100,000** 调度券原能。

### 交易规则
| 规则 | 说明 |
|------|------|
| 最小交易单位 | 1 股 |
| 价格精度 | 0.01 |
| 涨跌停限制 | 无 |
| 手续费 | 无 |
| 交易制度 | T+0（当日买入可当日卖出） |

### 价格波动
价格每 5 秒更新一次，波动范围 -2% 到 +2%。

---

## 微信登录配置

### 1. 注册微信开放平台账号

访问 [微信开放平台](https://open.weixin.qq.com) 注册开发者账号并完成认证。

### 2. 创建网站应用

1. 进入「管理中心」→「网站应用」→「创建网站应用」
2. 填写应用信息，上传网站图标
3. 设置网站域名（开发环境可填 localhost）
4. 设置授权回调域名（如：`localhost:8081`）

### 3. 获取 AppID 和 AppSecret

应用创建审核通过后，在应用详情页面获取：
- **AppID**: 微信应用唯一标识
- **AppSecret**: 微信应用密钥

### 4. 配置环境变量

```bash
# 微信配置
WECHAT_APP_ID=wx1234567890        # 替换为你的 AppID
WECHAT_APP_SECRET=your_secret     # 替换为你的 AppSecret
WECHAT_REDIRECT_URI=http://your-domain.com/api/auth/wx-callback
```

### 5. 微信登录流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   前端页面   │     │   后端服务   │     │   微信 API   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       │  1. 请求二维码    │                   │
       │ ───────────────>  │                   │
       │                   │                   │
       │                   │  2. 生成 OAuth 链接 │
       │                   │ ───────────────>  │
       │                   │                   │
       │  3. 返回二维码    │                   │
       │ <───────────────  │                   │
       │                   │                   │
       │  4. 用户微信扫码  │                   │
       │                   │                   │
       │                   │  5. 回调 code     │
       │                   │ <───────────────  │
       │                   │                   │
       │                   │  6. 换取 token    │
       │                   │ ───────────────>  │
       │                   │                   │
       │  7. 轮询成功      │                   │
       │ <───────────────  │                   │
       │                   │                   │
```

---

## 注意事项

1. **微信登录**: 需要微信开放平台账号，开发环境可使用快捷登录测试
2. **数据库**: 使用 Liquibase 自动迁移，首次启动自动创建表结构
3. **Redis**: Sa-Token 使用 Redis 存储 token，确保 Redis 服务正常运行
4. **并发处理**: 撮合引擎为简化实现，生产环境需要更完善的并发控制
5. **价格波动**: 当前为简单随机游走算法，可根据需要调整波动模型

---

## License

MIT License
