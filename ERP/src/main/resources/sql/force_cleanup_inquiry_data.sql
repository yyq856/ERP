-- 强制清理询价单测试数据脚本
-- 彻底删除所有询价单和项目数据，解决重复数据问题

-- 先删除所有询价单项目（包括重复的）
DELETE FROM erp_inquiry_item;

-- 删除所有询价单
DELETE FROM erp_inquiry;

-- 重置自增ID
ALTER TABLE erp_inquiry AUTO_INCREMENT = 1;

-- 查看清理结果
SELECT COUNT(*) as inquiry_count FROM erp_inquiry;
SELECT COUNT(*) as inquiry_item_count FROM erp_inquiry_item;

-- 显示表结构确认新字段已添加
SHOW COLUMNS FROM erp_inquiry_item;

-- 显示所有约束和索引
SHOW INDEX FROM erp_inquiry_item;

COMMIT;