# 终末地调度卷交易模拟系统

基于《明日方舟：终末地》游戏设定创建的调度卷交易模拟系统。

## 技术栈

### 后端
- **框架**: Spring Boot 3.0
- **JDK**: 17
- **权限**: Sa-Token
- **数据库**: MySQL / H2 (开发环境)
- **ORM**: Spring Data JPA
- **API 文档**: Knife4j (Swagger)
- **WebSocket**: Spring WebSocket

### 前端
- **框架**: React 18
- **构建工具**: Vite 5
- **UI 库**: Ant Design 5
- **图表**: Recharts
- **HTTP**: Axios

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
1. **行情中心**: 实时价格、买卖五档、涨跌幅
2. **交易委托**: 买入/卖出、价格/数量输入、委托记录
3. **持仓查询**: 持仓数量、成本价、盈亏计算
4. **用户管理**: 用户列表、状态管理、原能分配

### 权限系统
- **普通用户**: 持仓查看、交易委托、行情查看
- **管理员**: 用户管理、原能分配、数据统计

### 登录方式
- 微信二维码登录（模拟实现）
- 快捷登录（测试用）

## 快速开始

### 后端启动

```bash
cd stock-backend

# 开发环境（使用 H2 内存数据库）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境（需要 MySQL）
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

启动后访问：
- API 地址：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html
- H2 控制台（开发环境）：http://localhost:8080/api/h2-console

### 前端启动

```bash
cd stock-frontend

# 安装依赖
npm install

# 启动开发服务器
npm start
```

启动后访问：http://localhost:3000

## 项目结构

```
endfield-stock-dept/
├── stock-backend/           # Spring Boot 后端
│   ├── src/main/java/com/adrainty/stock/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 服务层
│   │   ├── repository/      # 数据访问层
│   │   ├── entity/          # 实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── enums/           # 枚举
│   │   ├── exception/       # 异常处理
│   │   └── util/            # 工具类
│   └── pom.xml
├── stock-frontend/          # React 前端
│   ├── src/
│   │   ├── pages/           # 页面组件
│   │   ├── layouts/         # 布局组件
│   │   ├── services/        # API 服务
│   │   └── utils/           # 工具函数
│   └── package.json
└── task.md                  # 开发任务列表
```

## API 接口

### 认证接口
- `POST /api/auth/wx-qrcode` - 获取微信二维码
- `GET /api/auth/wx-qrcode/{scene}` - 检查二维码状态
- `POST /api/auth/wx-login` - 微信 code 登录
- `POST /api/auth/logout` - 退出登录
- `GET /api/auth/user-info` - 获取用户信息

### 交易所接口
- `GET /api/exchange/list` - 获取所有交易所
- `GET /api/exchange/{exchangeId}/instruments` - 获取交易所品种

### 行情接口
- `GET /api/market/instruments` - 获取所有品种行情
- `GET /api/market/instruments/{exchangeId}` - 获取交易所品种
- `GET /api/market/instrument/{instrumentCode}` - 获取品种详情
- `GET /api/market/orderbook/{exchangeId}/{instrumentCode}` - 获取档口数据

### 交易接口
- `GET /api/trade/account/{exchangeId}` - 获取资金账户
- `GET /api/trade/position/{exchangeId}` - 获取持仓列表
- `POST /api/trade/order` - 下单委托
- `POST /api/trade/order/{orderNo}/cancel` - 撤单
- `GET /api/trade/orders` - 获取订单列表

### 管理员接口
- `POST /api/admin/allocate` - 分配原能
- `GET /api/admin/users` - 获取用户列表
- `GET /api/admin/user/{userId}` - 获取用户详情
- `POST /api/admin/user/{userId}/status` - 更新用户状态
- `GET /api/admin/allocations` - 获取分配记录
- `GET /api/admin/statistics` - 获取统计数据

## 游戏规则

### 初始资金
新用户注册后，每个交易所赠送 **100,000** 调度券原能。

### 交易规则
- 最小交易单位：1 股
- 价格精度：0.01
- 无涨跌停限制
- 无手续费
- T+0 交易（当日买入可当日卖出）

### 价格波动
价格每 5 秒更新一次，波动范围 -2% 到 +2%。

## 开发进度

| 阶段 | 状态 | 完成时间 |
|------|------|----------|
| 后端项目初始化 | ✅ 完成 | 2026-02-26 |
| 数据库设计 | ✅ 完成 | 2026-02-26 |
| Sa-Token 权限集成 | ✅ 完成 | 2026-02-26 |
| 微信二维码登录 | ✅ 完成 | 2026-02-26 |
| 交易所与行情模块 | ✅ 完成 | 2026-02-26 |
| 档口订单簿与 WebSocket | ✅ 完成 | 2026-02-26 |
| 交易撮合引擎 | ✅ 完成 | 2026-02-26 |
| 管理员功能 | ✅ 完成 | 2026-02-26 |
| 前端项目初始化 | ✅ 完成 | 2026-02-26 |
| 前端页面开发 | ✅ 完成 | 2026-02-26 |

## 注意事项

1. 微信登录功能为模拟实现，实际使用需要对接微信开放平台 API
2. 开发环境使用 H2 内存数据库，重启后数据清空
3. 撮合引擎简化实现，生产环境需要更完善的并发处理
4. 价格波动算法为简单随机游走，可根据需要调整

## License

MIT License
