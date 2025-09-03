-- 客户余额表
CREATE TABLE IF NOT EXISTS erp_customer_balance (
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    company_code VARCHAR(10) NOT NULL COMMENT '公司代码',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    currency VARCHAR(5) NOT NULL DEFAULT 'USD' COMMENT '货币',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (customer_id, company_code, currency),
    FOREIGN KEY (customer_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (currency) REFERENCES erp_currency(currency_code),
    
    INDEX idx_customer_company (customer_id, company_code),
    INDEX idx_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户余额表';

-- 插入测试数据
INSERT IGNORE INTO erp_customer_balance (customer_id, company_code, balance, currency) VALUES 
(1, '1000', 0.00, 'USD'),
(1, '1000', 0.00, 'EUR'),
(2, '1000', 0.00, 'USD'),
(2, '1000', 0.00, 'EUR');
