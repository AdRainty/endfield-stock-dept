# 数据库迁移说明

## 迁移概述

本项目已从 **Spring Data JPA + MySQL** 迁移到 **MyBatis Plus + PostgreSQL**。

## 主要变更

### 1. 依赖变更 (pom.xml)

**移除的依赖：**
- `spring-boot-starter-data-jpa`
- `mysql-connector-j`
- `h2`

**新增的依赖：**
- `mybatis-plus-spring-boot3-starter` (3.5.5)
- `postgresql`

### 2. 配置文件变更 (application.yml)

**数据源配置：**
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/endfield_stock?currentSchema=public
    username: postgres
    password: postgres
```

**MyBatis Plus 配置：**
```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.adrainty.stock.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

### 3. 实体类变更

所有实体类已从 JPA 注解迁移到 MyBatis Plus 注解：

| JPA 注解 | MyBatis Plus 注解 |
|---------|------------------|
| `@Entity` | `@TableName` |
| `@Table` | `@TableName` |
| `@Column` | `@TableField` |
| `@Id @GeneratedValue` | `@TableId(type = IdType.AUTO)` |
| `@MappedSuperclass` | 移除（MyBatis Plus 不需要） |

**BaseEntity 变更：**
- 移除了 `@EntityListeners(AuditingEntityListener.class)`
- 移除了 `@CreatedDate` 和 `@LastModifiedDate`
- MyBatis Plus 不自动处理审计字段，需要在插入/更新时手动设置

### 4. Repository 迁移到 Mapper

所有 Repository 接口已转换为 MyBatis Plus Mapper 接口：

| 原 Repository | 新 Mapper |
|-------------|----------|
| `UserRepository` | `UserMapper` |
| `ExchangeRepository` | `ExchangeMapper` |
| `InstrumentRepository` | `InstrumentMapper` |
| `UserPositionRepository` | `UserPositionMapper` |
| `OrderRepository` | `OrderMapper` |
| `TradeRecordRepository` | `TradeRecordMapper` |
| `CapitalFlowRepository` | `CapitalFlowMapper` |
| `AllocationRecordRepository` | `AllocationRecordMapper` |
| `PriceHistoryRepository` | `PriceHistoryMapper` |
| `RoleRepository` | `RoleMapper` |

**方法调用变更：**
| JPA 方法 | MyBatis Plus 方法 |
|---------|------------------|
| `repository.findById(id)` | `mapper.selectById(id)` |
| `repository.findAll()` | `mapper.selectList(null)` |
| `repository.save(entity)` | `mapper.insert(entity)` |
| `repository.update(entity)` | `mapper.updateById(entity)` |
| `repository.delete(id)` | `mapper.deleteById(id)` |

### 5. 服务类变更

所有服务类已更新以使用新的 Mapper 接口：
- `UserServiceImpl`
- `InstrumentServiceImpl`
- `PositionServiceImpl`
- `OrderServiceImpl`
- `CapitalServiceImpl`
- `AdminServiceImpl`
- `ExchangeServiceImpl`
- `MatchingEngineImpl`

### 6. 配置类变更

**移除的配置类：**
- `JpaConfig` (包含 `@EnableJpaAuditing`)

**新增的配置类：**
- `MybatisPlusConfig` (分页插件等)

### 7. 数据库脚本

新增了 PostgreSQL 数据库初始化脚本：
- `src/main/resources/db/init.sql`

包含：
- 所有表的 DDL 语句
- 索引创建语句
- 默认数据（交易所、角色）

## PostgreSQL 数据库初始化

执行以下命令初始化数据库：

```bash
psql -U postgres -d endfield_stock -f src/main/resources/db/init.sql
```

或者在 psql 中执行：
```sql
\i src/main/resources/db/init.sql
```

## 数据库表结构

| 表名 | 说明 |
|-----|------|
| `exchange` | 交易所表 |
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `instrument` | 调度券品种表 |
| `user_position` | 用户持仓表 |
| `order_book` | 委托订单表 |
| `trade_record` | 交易记录表 |
| `capital_flow` | 资金流水表 |
| `allocation_record` | 管理员分配记录表 |
| `price_history` | 价格历史表（K 线数据） |

## 注意事项

1. **审计字段**：MyBatis Plus 不自动处理 `created_at` 和 `updated_at`，需要在插入和更新时手动设置
2. **主键生成**：使用 PostgreSQL 的 `BIGSERIAL` 类型自动递增主键
3. **驼峰命名**：配置文件中已启用 `map-underscore-to-camel-case`，自动映射下划线字段到驼峰属性
4. **事务管理**：`@Transactional` 注解保持不变，Spring 事务管理仍然有效

## 测试验证

启动应用后，验证以下功能：
1. 数据库连接正常
2. 用户微信登录功能
3. 交易所数据初始化
4. 品种数据初始化
5. 持仓查询功能
6. 订单功能

## 回滚方案

如需回滚到 JPA + MySQL：
1. 恢复 `pom.xml` 中的 JPA 和 MySQL 依赖
2. 恢复实体类的 JPA 注解
3. 恢复 Repository 接口
4. 恢复服务类中的 Repository 引用
5. 更新数据库连接配置
