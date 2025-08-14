-- 清理询价单测试数据脚本
-- 删除所有测试询价单和项目数据，确保干净的测试环境

-- 删除所有询价单项目
DELETE FROM erp_inquiry_item;

-- 删除所有询价单
DELETE FROM erp_inquiry;

-- 重置自增ID（如果需要）
-- ALTER TABLE erp_inquiry AUTO_INCREMENT = 1;

-- 查看清理结果
SELECT COUNT(*) as inquiry_count FROM erp_inquiry;
SELECT COUNT(*) as inquiry_item_count FROM erp_inquiry_item;

-- 显示表结构确认新字段已添加
DESCRIBE erp_inquiry_item;