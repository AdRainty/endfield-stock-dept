-- =====================================================
-- 终末地调度卷交易模拟系统 - 初始化数据脚本
-- MySQL 版本
-- =====================================================

-- 初始化交易所数据
INSERT INTO exchange (exchange_code, name, description, status, trading_start, trading_end)
VALUES 
    ('VALLEY', '四号谷底', '位于四号谷底的交易所，主要交易能源类调度券', 1, '00:00', '23:59'),
    ('WULING', '武陵', '位于武陵地区的交易所，主要交易技术类调度券', 1, '00:00', '23:59')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 初始化调度券品种数据
-- 四号谷底交易所品种
INSERT INTO instrument (instrument_code, name, exchange_id, current_price, prev_close_price, open_price, high_price, low_price, change_percent, change_amount, volume, turnover, status)
VALUES 
    ('VL_ENERGY', '能源调度券', 1, 100.00, 100.00, 100.00, 100.00, 100.00, 0, 0, 0, 0, 1),
    ('VL_MATERIAL', '材料调度券', 1, 50.00, 50.00, 50.00, 50.00, 50.00, 0, 0, 0, 0, 1),
    ('VL_DATA', '数据调度券', 1, 75.00, 75.00, 75.00, 75.00, 75.00, 0, 0, 0, 0, 1),
    ('VL_TECH', '技术调度券', 1, 120.00, 120.00, 120.00, 120.00, 120.00, 0, 0, 0, 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 武陵交易所品种
INSERT INTO instrument (instrument_code, name, exchange_id, current_price, prev_close_price, open_price, high_price, low_price, change_percent, change_amount, volume, turnover, status)
VALUES 
    ('WL_ENERGY', '能源调度券', 2, 100.00, 100.00, 100.00, 100.00, 100.00, 0, 0, 0, 0, 1),
    ('WL_MATERIAL', '材料调度券', 2, 50.00, 50.00, 50.00, 50.00, 50.00, 0, 0, 0, 0, 1),
    ('WL_DATA', '数据调度券', 2, 75.00, 75.00, 75.00, 75.00, 75.00, 0, 0, 0, 0, 1),
    ('WL_TECH', '技术调度券', 2, 120.00, 120.00, 120.00, 120.00, 120.00, 0, 0, 0, 0, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description, status)
VALUES 
    ('USER', '普通用户', '可以进行交易、查看行情和持仓', 1),
    ('ADMIN', '管理员', '可以分配原能、管理用户', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
