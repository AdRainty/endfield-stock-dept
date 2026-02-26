-- 修复 instrument 表中 numeric 字段精度问题
-- change_percent 和 change_amount 需要更大的整数部分

-- 修改 change_percent 字段：从 NUMERIC(6,4) 改为 NUMERIC(10,4)
-- 允许最大值达到 999999.9999%（理论上足够用）
ALTER TABLE instrument
    ALTER COLUMN change_percent TYPE NUMERIC(10,4);

-- 修改 change_amount 字段：从 NUMERIC(6,2) 改为 NUMERIC(14,2)
-- 允许更大的涨跌额
ALTER TABLE instrument
    ALTER COLUMN change_amount TYPE NUMERIC(14,2);

-- 修改 current_price 字段：确保足够的精度
ALTER TABLE instrument
    ALTER COLUMN current_price TYPE NUMERIC(14,4);

ALTER TABLE instrument
    ALTER COLUMN prev_close_price TYPE NUMERIC(14,4);

ALTER TABLE instrument
    ALTER COLUMN open_price TYPE NUMERIC(14,4);

ALTER TABLE instrument
    ALTER COLUMN high_price TYPE NUMERIC(14,4);

ALTER TABLE instrument
    ALTER COLUMN low_price TYPE NUMERIC(14,4);

-- 添加 type 字段（如果不存在）
ALTER TABLE instrument
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'STOCK';

-- 添加 exchange 表的 type 字段（如果需要）
ALTER TABLE exchange
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);
