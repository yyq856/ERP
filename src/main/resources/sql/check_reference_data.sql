-- 检查参考表数据是否存在
-- 执行这些查询来检查参考数据的状态

SELECT 'erp_customer_title' as table_name, COUNT(*) as count FROM erp_customer_title
UNION ALL
SELECT 'erp_language', COUNT(*) FROM erp_language
UNION ALL
SELECT 'erp_company_code', COUNT(*) FROM erp_company_code
UNION ALL
SELECT 'erp_reconciliation_account', COUNT(*) FROM erp_reconciliation_account
UNION ALL
SELECT 'erp_sort_key', COUNT(*) FROM erp_sort_key
UNION ALL
SELECT 'erp_sales_org', COUNT(*) FROM erp_sales_org
UNION ALL
SELECT 'erp_distribution_channel', COUNT(*) FROM erp_distribution_channel
UNION ALL
SELECT 'erp_division', COUNT(*) FROM erp_division
UNION ALL
SELECT 'erp_currency', COUNT(*) FROM erp_currency
UNION ALL
SELECT 'erp_sales_district', COUNT(*) FROM erp_sales_district
UNION ALL
SELECT 'erp_price_group', COUNT(*) FROM erp_price_group
UNION ALL
SELECT 'erp_customer_group', COUNT(*) FROM erp_customer_group
UNION ALL
SELECT 'erp_deliver_priority', COUNT(*) FROM erp_deliver_priority
UNION ALL
SELECT 'erp_shipping_condition', COUNT(*) FROM erp_shipping_condition
UNION ALL
SELECT 'erp_plant_name', COUNT(*) FROM erp_plant_name
UNION ALL
SELECT 'erp_payment_terms', COUNT(*) FROM erp_payment_terms
UNION ALL
SELECT 'erp_acct', COUNT(*) FROM erp_acct;
