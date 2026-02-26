-- PostgreSQL 数据库初始化脚本
-- 终末地调度卷交易模拟系统

-- 创建交易所表
CREATE TABLE IF NOT EXISTS exchange (
    id BIGSERIAL PRIMARY KEY,
    exchange_code VARCHAR(20) UNIQUE,
    name VARCHAR(50),
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    trading_start VARCHAR(10) DEFAULT '00:00',
    trading_end VARCHAR(10) DEFAULT '23:59',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    wechat_openid VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    status INTEGER DEFAULT 1,
    register_ip VARCHAR(50),
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(20) UNIQUE,
    role_name VARCHAR(50),
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建调度券品种表
CREATE TABLE IF NOT EXISTS instrument (
    id BIGSERIAL PRIMARY KEY,
    instrument_code VARCHAR(20) UNIQUE,
    name VARCHAR(50),
    exchange_id BIGINT,
    current_price DECIMAL(10, 2),
    prev_close_price DECIMAL(10, 2),
    open_price DECIMAL(10, 2),
    high_price DECIMAL(10, 2),
    low_price DECIMAL(10, 2),
    change_percent DECIMAL(6, 4),
    change_amount DECIMAL(10, 2),
    volume BIGINT DEFAULT 0,
    turnover DECIMAL(20, 2),
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户持仓表
CREATE TABLE IF NOT EXISTS user_position (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    instrument_code VARCHAR(20) NOT NULL,
    quantity DECIMAL(20, 2) DEFAULT 0,
    available_quantity DECIMAL(20, 2) DEFAULT 0,
    frozen_quantity DECIMAL(20, 2) DEFAULT 0,
    cost_price DECIMAL(10, 2),
    cost_amount DECIMAL(20, 2) DEFAULT 0,
    latest_price DECIMAL(10, 2),
    profit_loss DECIMAL(20, 2) DEFAULT 0,
    profit_loss_rate DECIMAL(6, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_exchange_instrument UNIQUE (user_id, exchange_id, instrument_code)
);

-- 创建委托订单表
CREATE TABLE IF NOT EXISTS order_book (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    instrument_code VARCHAR(20) NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(20, 2) NOT NULL,
    filled_quantity DECIMAL(20, 2) DEFAULT 0,
    unfilled_quantity DECIMAL(20, 2),
    filled_amount DECIMAL(20, 2) DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    order_time TIMESTAMP,
    filled_time TIMESTAMP,
    cancelled_time TIMESTAMP,
    cancel_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建交易记录表
CREATE TABLE IF NOT EXISTS trade_record (
    id BIGSERIAL PRIMARY KEY,
    trade_no VARCHAR(32) UNIQUE NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    buyer_user_id BIGINT NOT NULL,
    seller_user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    instrument_code VARCHAR(20) NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(20, 2) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    fee DECIMAL(10, 2) DEFAULT 0,
    trade_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建资金流水表
CREATE TABLE IF NOT EXISTS capital_flow (
    id BIGSERIAL PRIMARY KEY,
    flow_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    flow_type VARCHAR(20) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    balance_after DECIMAL(20, 2) NOT NULL,
    ref_no VARCHAR(32),
    remark VARCHAR(255),
    operate_time TIMESTAMP,
    operator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建管理员分配记录表
CREATE TABLE IF NOT EXISTS allocation_record (
    id BIGSERIAL PRIMARY KEY,
    allocation_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    balance_after DECIMAL(20, 2) NOT NULL,
    reason VARCHAR(255),
    admin_user_id BIGINT NOT NULL,
    operate_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建价格历史表（用于 K 线数据）
CREATE TABLE IF NOT EXISTS price_history (
    id BIGSERIAL PRIMARY KEY,
    exchange_id BIGINT NOT NULL,
    instrument_code VARCHAR(20) NOT NULL,
    period VARCHAR(10) NOT NULL,
    trade_time TIMESTAMP NOT NULL,
    open_price DECIMAL(10, 2) NOT NULL,
    high_price DECIMAL(10, 2) NOT NULL,
    low_price DECIMAL(10, 2) NOT NULL,
    close_price DECIMAL(10, 2) NOT NULL,
    volume DECIMAL(20, 2),
    turnover DECIMAL(20, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_exchange_instrument_period_time UNIQUE (exchange_id, instrument_code, period, trade_time)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_position_user_exchange ON user_position(user_id, exchange_id);
CREATE INDEX IF NOT EXISTS idx_order_book_user_id ON order_book(user_id);
CREATE INDEX IF NOT EXISTS idx_order_book_order_time ON order_book(order_time DESC);
CREATE INDEX IF NOT EXISTS idx_trade_record_buyer ON trade_record(buyer_user_id);
CREATE INDEX IF NOT EXISTS idx_trade_record_seller ON trade_record(seller_user_id);
CREATE INDEX IF NOT EXISTS idx_capital_flow_user_id ON capital_flow(user_id);
CREATE INDEX IF NOT EXISTS idx_allocation_record_user_id ON allocation_record(user_id);
CREATE INDEX IF NOT EXISTS idx_price_history_exchange_instrument ON price_history(exchange_id, instrument_code);

-- 插入默认交易所数据
INSERT INTO exchange (exchange_code, name, description, status, trading_start, trading_end)
VALUES
    ('VALLEY', '四号谷底', '位于四号谷底的交易所，主要交易能源类调度券', 1, '00:00', '23:59'),
    ('WULING', '武陵', '位于武陵地区的交易所，主要交易技术类调度券', 1, '00:00', '23:59')
ON CONFLICT (exchange_code) DO NOTHING;

-- 插入默认角色数据
INSERT INTO sys_role (role_code, role_name, description, status)
VALUES
    ('ADMIN', '管理员', '系统管理员', 1),
    ('USER', '普通用户', '普通交易用户', 1)
ON CONFLICT (role_code) DO NOTHING;
