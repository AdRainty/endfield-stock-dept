-- =====================================================
-- 终末地调度卷交易模拟系统 - 数据库初始化脚本
-- MySQL 版本
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    wechat_openid VARCHAR(100) UNIQUE COMMENT '微信 OpenID',
    nickname VARCHAR(50) COMMENT '用户昵称',
    avatar VARCHAR(255) COMMENT '用户头像',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色：USER-普通用户，ADMIN-管理员',
    status INT DEFAULT 1 COMMENT '账户状态：1-正常 0-禁用',
    register_ip VARCHAR(50) COMMENT '注册 IP',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录 IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wechat_openid (wechat_openid),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    role_code VARCHAR(20) UNIQUE NOT NULL COMMENT '角色代码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status INT DEFAULT 1 COMMENT '角色状态：1-正常 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 交易所表
CREATE TABLE IF NOT EXISTS exchange (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    exchange_code VARCHAR(20) UNIQUE NOT NULL COMMENT '交易所代码',
    name VARCHAR(50) NOT NULL COMMENT '交易所名称',
    description VARCHAR(255) COMMENT '交易所描述',
    status INT DEFAULT 1 COMMENT '交易所状态：1-正常 0-维护',
    trading_start VARCHAR(10) DEFAULT '00:00' COMMENT '交易开始时间',
    trading_end VARCHAR(10) DEFAULT '23:59' COMMENT '交易结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易所表';

-- 调度券品种表
CREATE TABLE IF NOT EXISTS instrument (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    instrument_code VARCHAR(20) UNIQUE NOT NULL COMMENT '品种代码',
    name VARCHAR(50) NOT NULL COMMENT '品种名称',
    exchange_id BIGINT NOT NULL COMMENT '所属交易所 ID',
    current_price DECIMAL(10,2) DEFAULT 0 COMMENT '当前价格',
    prev_close_price DECIMAL(10,2) DEFAULT 0 COMMENT '昨日收盘价',
    open_price DECIMAL(10,2) DEFAULT 0 COMMENT '今日开盘价',
    high_price DECIMAL(10,2) DEFAULT 0 COMMENT '最高价',
    low_price DECIMAL(10,2) DEFAULT 0 COMMENT '最低价',
    change_percent DECIMAL(6,4) DEFAULT 0 COMMENT '涨跌幅',
    change_amount DECIMAL(10,2) DEFAULT 0 COMMENT '涨跌额',
    volume BIGINT DEFAULT 0 COMMENT '成交量',
    turnover DECIMAL(20,2) DEFAULT 0 COMMENT '成交额',
    status INT DEFAULT 1 COMMENT '状态：1-交易中 0-休市',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_exchange_id (exchange_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度券品种表';

-- 委托订单表
CREATE TABLE IF NOT EXISTS order_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    instrument_code VARCHAR(20) NOT NULL COMMENT '品种代码',
    order_type VARCHAR(10) NOT NULL COMMENT '订单类型：BUY-买入 SELL-卖出',
    price DECIMAL(10,2) NOT NULL COMMENT '委托价格',
    quantity DECIMAL(20,2) NOT NULL COMMENT '委托数量',
    filled_quantity DECIMAL(20,2) DEFAULT 0 COMMENT '已成交数量',
    unfilled_quantity DECIMAL(20,2) DEFAULT 0 COMMENT '未成交数量',
    filled_amount DECIMAL(20,2) DEFAULT 0 COMMENT '成交金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    order_time DATETIME COMMENT '委托时间',
    filled_time DATETIME COMMENT '成交时间',
    cancelled_time DATETIME COMMENT '撤单时间',
    cancel_reason VARCHAR(255) COMMENT '撤单原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='委托订单表';

-- 用户持仓表
CREATE TABLE IF NOT EXISTS user_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    instrument_code VARCHAR(20) NOT NULL COMMENT '品种代码',
    quantity DECIMAL(20,2) DEFAULT 0 COMMENT '持仓数量',
    available_quantity DECIMAL(20,2) DEFAULT 0 COMMENT '可用数量',
    frozen_quantity DECIMAL(20,2) DEFAULT 0 COMMENT '冻结数量',
    cost_price DECIMAL(10,2) DEFAULT 0 COMMENT '持仓成本价',
    cost_amount DECIMAL(20,2) DEFAULT 0 COMMENT '持仓成本总额',
    latest_price DECIMAL(10,2) DEFAULT 0 COMMENT '最新价',
    profit_loss DECIMAL(20,2) DEFAULT 0 COMMENT '持仓盈亏',
    profit_loss_rate DECIMAL(6,4) DEFAULT 0 COMMENT '盈亏比例',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_exchange_instrument (user_id, exchange_id, instrument_code),
    INDEX idx_user_id (user_id),
    INDEX idx_exchange_id (exchange_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户持仓表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS trade_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    trade_no VARCHAR(32) UNIQUE NOT NULL COMMENT '交易流水号',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    buyer_user_id BIGINT NOT NULL COMMENT '买方用户 ID',
    seller_user_id BIGINT NOT NULL COMMENT '卖方用户 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    instrument_code VARCHAR(20) NOT NULL COMMENT '品种代码',
    order_type VARCHAR(10) NOT NULL COMMENT '交易类型',
    price DECIMAL(10,2) NOT NULL COMMENT '成交价格',
    quantity DECIMAL(20,2) NOT NULL COMMENT '成交数量',
    amount DECIMAL(20,2) NOT NULL COMMENT '成交金额',
    fee DECIMAL(10,2) DEFAULT 0 COMMENT '手续费',
    trade_time DATETIME COMMENT '交易时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_buyer_user_id (buyer_user_id),
    INDEX idx_seller_user_id (seller_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 资金流水表
CREATE TABLE IF NOT EXISTS capital_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    flow_no VARCHAR(32) UNIQUE NOT NULL COMMENT '流水号',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    flow_type VARCHAR(20) NOT NULL COMMENT '流水类型',
    amount DECIMAL(20,2) NOT NULL COMMENT '变动金额',
    balance_after DECIMAL(20,2) NOT NULL COMMENT '变动后余额',
    ref_no VARCHAR(32) COMMENT '关联业务单号',
    remark VARCHAR(255) COMMENT '备注',
    operate_time DATETIME COMMENT '操作时间',
    operator_id BIGINT COMMENT '操作人 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_exchange_id (exchange_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- 管理员分配记录表
CREATE TABLE IF NOT EXISTS allocation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    allocation_no VARCHAR(32) UNIQUE NOT NULL COMMENT '分配单号',
    user_id BIGINT NOT NULL COMMENT '目标用户 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    amount DECIMAL(20,2) NOT NULL COMMENT '分配金额',
    balance_after DECIMAL(20,2) NOT NULL COMMENT '分配后余额',
    reason VARCHAR(255) COMMENT '分配原因',
    admin_user_id BIGINT NOT NULL COMMENT '操作管理员 ID',
    operate_time DATETIME COMMENT '操作时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_allocation_no (allocation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员分配记录表';

-- 价格历史表（K 线数据）
CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    exchange_id BIGINT NOT NULL COMMENT '交易所 ID',
    instrument_code VARCHAR(20) NOT NULL COMMENT '品种代码',
    period VARCHAR(10) NOT NULL COMMENT 'K 线周期',
    trade_time DATETIME NOT NULL COMMENT '交易时间',
    open_price DECIMAL(10,2) NOT NULL COMMENT '开盘价',
    high_price DECIMAL(10,2) NOT NULL COMMENT '最高价',
    low_price DECIMAL(10,2) NOT NULL COMMENT '最低价',
    close_price DECIMAL(10,2) NOT NULL COMMENT '收盘价',
    volume DECIMAL(20,2) DEFAULT 0 COMMENT '成交量',
    turnover DECIMAL(20,2) DEFAULT 0 COMMENT '成交额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_exchange_instrument_period_time (exchange_id, instrument_code, period, trade_time),
    INDEX idx_instrument_period (instrument_code, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格历史表';
