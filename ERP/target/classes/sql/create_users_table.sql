CREATE TABLE erp_company_code (
    code VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (code)
);

CREATE TABLE erp_language (
    lang_id VARCHAR(5) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (lang_id)
);

CREATE TABLE erp_currency (
    currency_code VARCHAR(5) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (currency_code)
);

CREATE TABLE erp_customer_title (
    title_id VARCHAR(10) NOT NULL,
    title_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (title_id)
);

CREATE TABLE erp_title (
    title_id VARCHAR(10) NOT NULL,
    title_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (title_id)
);

CREATE TABLE erp_sort_key (
    key_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (key_id)
);

CREATE TABLE erp_payment_terms (
    term_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (term_id)
);

CREATE TABLE erp_sales_org (
    org_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (org_id)
);

CREATE TABLE erp_distribution_channel (
    channel_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (channel_id)
);

CREATE TABLE erp_division (
    division_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (division_id)
);

CREATE TABLE erp_sales_district (
    district_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (district_id)
);

CREATE TABLE erp_price_group (
    group_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id)
);

CREATE TABLE erp_customer_group (
    group_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id)
);

CREATE TABLE erp_deliver_priority (
    priority_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (priority_id)
);

CREATE TABLE erp_shipping_condition (
    condition_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (condition_id)
);

CREATE TABLE erp_acct (
    acct_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (acct_id)
);

CREATE TABLE erp_reconciliation_account (
    account_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (account_id)
);

CREATE TABLE erp_plant_name (
    plant_id BIGINT NOT NULL,
    plant_name VARCHAR(50) NOT NULL,
    city VARCHAR(50),
    PRIMARY KEY (plant_id)
);

CREATE TABLE erp_storage_location (
    loc_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (loc_id)
);

CREATE TABLE erp_order_status (
    status_code VARCHAR(10) NOT NULL,
    description VARCHAR(50) NOT NULL,
    PRIMARY KEY (status_code)
);

CREATE TABLE erp_management (
    level_id INT NOT NULL,
    description VARCHAR(50) NOT NULL,
    PRIMARY KEY (level_id)
);

CREATE TABLE erp_department (
    dept_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (dept_id)
);

CREATE TABLE erp_function (
    function_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (function_id)
);

CREATE TABLE erp_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';


CREATE TABLE erp_customer (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户编号',
    title VARCHAR(10) NOT NULL COMMENT '公司类型（GR/PI）',
    name VARCHAR(60) NOT NULL COMMENT '公司名称',
    language VARCHAR(5) NOT NULL,
    street VARCHAR(60) NOT NULL,
    city VARCHAR(60) NOT NULL,
    region CHAR(2) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    country CHAR(2) NOT NULL,
    company_code VARCHAR(10) NOT NULL,
    reconciliation_account VARCHAR(10) NOT NULL,
    sort_key VARCHAR(10) NOT NULL,
    sales_org VARCHAR(10) NOT NULL,
    channel INT NOT NULL,
    division VARCHAR(10) NOT NULL,
    currency VARCHAR(5) NOT NULL,
    sales_district VARCHAR(10) NOT NULL,
    price_group VARCHAR(10) NOT NULL,
    customer_group VARCHAR(10) NOT NULL,
    delivery_priority VARCHAR(10) NOT NULL,
    shipping_condition VARCHAR(10) NOT NULL,
    delivering_plant BIGINT NOT NULL,
    max_part_deliv INT NOT NULL,
    incoterms VARCHAR(10) NOT NULL,
    incoterms_location VARCHAR(20) NOT NULL,
    payment_terms VARCHAR(10) NOT NULL,
    acct_assignment VARCHAR(10) NOT NULL,
    output_tax INT NOT NULL,
    
    FOREIGN KEY (title) REFERENCES erp_customer_title(title_id),
    FOREIGN KEY (language) REFERENCES erp_language(lang_id),
    FOREIGN KEY (company_code) REFERENCES erp_company_code(code),
    FOREIGN KEY (reconciliation_account) REFERENCES erp_reconciliation_account(account_id),
    FOREIGN KEY (sort_key) REFERENCES erp_sort_key(key_id),
    FOREIGN KEY (sales_org) REFERENCES erp_sales_org(org_id),
    FOREIGN KEY (channel) REFERENCES erp_distribution_channel(channel_id),
    FOREIGN KEY (division) REFERENCES erp_division(division_id),
    FOREIGN KEY (currency) REFERENCES erp_currency(currency_code),
    FOREIGN KEY (sales_district) REFERENCES erp_sales_district(district_id),
    FOREIGN KEY (price_group) REFERENCES erp_price_group(group_id),
    FOREIGN KEY (customer_group) REFERENCES erp_customer_group(group_id),
    FOREIGN KEY (delivery_priority) REFERENCES erp_deliver_priority(priority_id),
    FOREIGN KEY (shipping_condition) REFERENCES erp_shipping_condition(condition_id),
    FOREIGN KEY (delivering_plant) REFERENCES erp_plant_name(plant_id),
    FOREIGN KEY (payment_terms) REFERENCES erp_payment_terms(term_id),
    FOREIGN KEY (acct_assignment) REFERENCES erp_acct(acct_id)
);

CREATE TABLE erp_contact (
    contact_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '联系人ID',
    title VARCHAR(10) NOT NULL COMMENT '称谓（Mr/Ms）',
    first_name VARCHAR(20) NOT NULL COMMENT '名',
    last_name VARCHAR(30) NOT NULL COMMENT '姓',
    cor_language VARCHAR(5) NOT NULL,
    country CHAR(2) NOT NULL,
    
    FOREIGN KEY (title) REFERENCES erp_title(title_id),
    FOREIGN KEY (cor_language) REFERENCES erp_language(lang_id)
);

CREATE TABLE erp_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系索引',
    rel_category VARCHAR(30) NOT NULL COMMENT '关系类别',
    bp1 BIGINT NOT NULL COMMENT '客户ID',
    bp2 BIGINT NOT NULL COMMENT '联系人ID',
    management INT NOT NULL COMMENT 'VIP等级',
    department VARCHAR(10) NOT NULL,
    function VARCHAR(10) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    FOREIGN KEY (bp1) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (bp2) REFERENCES erp_contact(contact_id),
    FOREIGN KEY (management) REFERENCES erp_management(level_id),
    FOREIGN KEY (department) REFERENCES erp_department(dept_id),
    FOREIGN KEY (function) REFERENCES erp_function(function_id)
);

CREATE TABLE erp_material (
    mat_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    mat_desc VARCHAR(60) NOT NULL COMMENT '物料描述',
    division CHAR(2) NOT NULL COMMENT '产品线',
    base_uom VARCHAR(60) NOT NULL COMMENT '基本单位',
    srd_price FLOAT NOT NULL COMMENT '标准价格'
);

CREATE TABLE erp_stock (
    plant_id BIGINT NOT NULL COMMENT '工厂ID',
    mat_id BIGINT NOT NULL COMMENT '物料ID',
    bp_id BIGINT NOT NULL COMMENT '客户ID',
    storage_loc VARCHAR(10) NOT NULL COMMENT '库存地点',
    qty_on_hand FLOAT NOT NULL COMMENT '在手量',
    qty_committed FLOAT NOT NULL COMMENT '承诺量',
    PRIMARY KEY (plant_id, mat_id, bp_id, storage_loc),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (bp_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (storage_loc) REFERENCES erp_storage_location(loc_id)
);

CREATE TABLE erp_inquiry (
    inquiry_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '询价单ID',
    cust_id BIGINT NOT NULL,
    inquiry_type VARCHAR(10),
    sls_org VARCHAR(10),
    sales_district VARCHAR(10),
    division VARCHAR(10),
    sold_tp BIGINT NOT NULL,
    ship_tp BIGINT NOT NULL,
    cust_ref VARCHAR(30),
    customer_reference_date DATE,
    valid_from_date DATE,
    valid_to_date DATE,
    probability FLOAT,
    net_value FLOAT,
    status VARCHAR(10),
    
    FOREIGN KEY (cust_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (sls_org) REFERENCES erp_sales_org(org_id),
    FOREIGN KEY (sales_district) REFERENCES erp_sales_district(district_id),
    FOREIGN KEY (division) REFERENCES erp_division(division_id),
    FOREIGN KEY (sold_tp) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (ship_tp) REFERENCES erp_customer(customer_id)
);

CREATE TABLE erp_inquiry_item (
    inquiry_id BIGINT NOT NULL,
    item_no SMALLINT NOT NULL,
    mat_id BIGINT NOT NULL,
    quantity SMALLINT NOT NULL,
    net_price FLOAT NOT NULL,
    item_value FLOAT NOT NULL,
    plant_id BIGINT NOT NULL,
    su VARCHAR(10) NOT NULL,
    
    PRIMARY KEY (inquiry_id, item_no),
    FOREIGN KEY (inquiry_id) REFERENCES erp_inquiry(inquiry_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id)
);

CREATE TABLE erp_quotation (
    quotation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报价单ID',
    reference_inquiry_id BIGINT,
    cust_id BIGINT NOT NULL,
    inquiry_type VARCHAR(10),
    sls_org VARCHAR(10),
    sales_district VARCHAR(10),
    division VARCHAR(10),
    sold_tp BIGINT,
    ship_tp BIGINT,
    cust_ref VARCHAR(30),
    customer_reference_date DATE,
    valid_from_date DATE,
    valid_to_date DATE,
    probability FLOAT,
    net_value FLOAT,
    status VARCHAR(10),
    
    FOREIGN KEY (reference_inquiry_id) REFERENCES erp_inquiry(inquiry_id),
    FOREIGN KEY (cust_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (sls_org) REFERENCES erp_sales_org(org_id),
    FOREIGN KEY (sales_district) REFERENCES erp_sales_district(district_id),
    FOREIGN KEY (division) REFERENCES erp_division(division_id),
    FOREIGN KEY (sold_tp) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (ship_tp) REFERENCES erp_customer(customer_id)
);

CREATE TABLE erp_quotation_item (
    quotation_id BIGINT NOT NULL,
    item_no SMALLINT NOT NULL,
    mat_id BIGINT NOT NULL,
    quantity SMALLINT NOT NULL,
    net_price FLOAT NOT NULL,
    item_discount_pct SMALLINT NOT NULL,
    item_value FLOAT NOT NULL,
    plant_id BIGINT NOT NULL,
    su VARCHAR(10) NOT NULL,
    cnty SMALLINT NOT NULL,
    
    PRIMARY KEY (quotation_id, item_no),
    FOREIGN KEY (quotation_id) REFERENCES erp_quotation(quotation_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id)
);

CREATE TABLE erp_sales_order_hdr (
    so_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销售订单ID',
    quote_id BIGINT,
    customer_no BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    doc_date DATE,
    req_delivery_date DATE,
    currency VARCHAR(5),
    net_value FLOAT,
    tax_value FLOAT,
    gross_value FLOAT,
    incoterms VARCHAR(10),
    payment_terms VARCHAR(10),
    
    FOREIGN KEY (quote_id) REFERENCES erp_quotation(quotation_id),
    FOREIGN KEY (customer_no) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (contact_id) REFERENCES erp_contact(contact_id),
    FOREIGN KEY (currency) REFERENCES erp_currency(currency_code),
    FOREIGN KEY (payment_terms) REFERENCES erp_payment_terms(term_id)
);

CREATE TABLE erp_sales_item (
    so_id BIGINT NOT NULL,
    item_no SMALLINT NOT NULL,
    mat_id BIGINT NOT NULL,
    plt_id BIGINT NOT NULL,
    storage_loc VARCHAR(10) NOT NULL,
    quantity SMALLINT NOT NULL,
    su VARCHAR(10) NOT NULL,
    net_price FLOAT NOT NULL,
    discount_pct SMALLINT NOT NULL,
    status VARCHAR(10) NOT NULL,
    
    PRIMARY KEY (so_id, item_no),
    FOREIGN KEY (so_id) REFERENCES erp_sales_order_hdr(so_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (plt_id) REFERENCES erp_plant_name(plant_id),
    FOREIGN KEY (storage_loc) REFERENCES erp_storage_location(loc_id),
    FOREIGN KEY (status) REFERENCES erp_order_status(status_code)
);

CREATE TABLE erp_outbound_delivery (
    dlv_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交货单ID',
    so_id BIGINT NOT NULL,
    ship_tp BIGINT NOT NULL,
    shipping_point VARCHAR(10),
    pick_date_plan DATE,
    gi_date DATE,
    status VARCHAR(10),
    
    FOREIGN KEY (so_id) REFERENCES erp_sales_order_hdr(so_id),
    FOREIGN KEY (ship_tp) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (status) REFERENCES erp_order_status(status_code)
);

CREATE TABLE erp_outbound_delivery_item (
    dlv_id BIGINT NOT NULL,
    item_no SMALLINT NOT NULL,
    mat_id BIGINT NOT NULL,
    pick_quantity SMALLINT NOT NULL,
    plant_id BIGINT NOT NULL,
    storage_loc VARCHAR(10) NOT NULL,
    
    PRIMARY KEY (dlv_id, item_no),
    FOREIGN KEY (dlv_id) REFERENCES erp_outbound_delivery(dlv_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id),
    FOREIGN KEY (storage_loc) REFERENCES erp_storage_location(loc_id)
);

CREATE TABLE erp_good_issue (
    gi_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发货ID',
    dlv_id BIGINT NOT NULL,
    posting_date DATE,
    mat_id BIGINT,
    quantity SMALLINT,
    plant_id BIGINT,
    
    FOREIGN KEY (dlv_id) REFERENCES erp_outbound_delivery(dlv_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id)
);

CREATE TABLE erp_billing_hdr (
    bill_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发票ID',
    dlv_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    billing_date DATE,
    net FLOAT,
    tax FLOAT,
    gross FLOAT,
    status VARCHAR(10),
    
    FOREIGN KEY (dlv_id) REFERENCES erp_outbound_delivery(dlv_id),
    FOREIGN KEY (customer_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (status) REFERENCES erp_order_status(status_code)
);

CREATE TABLE erp_billing_item (
    bill_id BIGINT NOT NULL,
    item_no SMALLINT NOT NULL,
    mat_id BIGINT NOT NULL,
    quantity SMALLINT NOT NULL,
    net_price FLOAT NOT NULL,
    tax_rate SMALLINT NOT NULL,
    
    PRIMARY KEY (bill_id, item_no),
    FOREIGN KEY (bill_id) REFERENCES erp_billing_hdr(bill_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id)
);

CREATE TABLE erp_payment (
    pay_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '付款ID',
    bill_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    posting_date DATE,
    amount FLOAT,
    currency VARCHAR(5),
    clearing_status VARCHAR(20),
    
    FOREIGN KEY (bill_id) REFERENCES erp_billing_hdr(bill_id),
    FOREIGN KEY (customer_id) REFERENCES erp_customer(customer_id),
    FOREIGN KEY (currency) REFERENCES erp_currency(currency_code)
);
