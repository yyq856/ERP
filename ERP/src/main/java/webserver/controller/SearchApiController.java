package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/search")
public class SearchApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, Object> ok(List<Map<String, Object>> data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }

    // 1) 通用搜索（简化：仅处理 include.contains -> LIKE）
    @PostMapping("/general/{type}")
    public Map<String, Object> generalSearch(@PathVariable("type") String type,
                                             @RequestBody(required = false) Map<String, Object> body) {
        String column;
        String table;
        switch (type) {
            case "plantName":
                table = "erp_plant_name"; column = "plant_name"; break;
            case "materialDescription":
                table = "erp_material"; column = "mat_desc"; break;
            case "material":
                table = "erp_material"; column = "mat_id"; break;
            case "countryKey":
                table = "erp_customer"; column = "country"; break;
            default:
                return ok(Collections.emptyList());
        }
        StringBuilder sql = new StringBuilder("SELECT DISTINCT " + column + " AS result FROM " + table + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null && body.get("include") instanceof Map) {
            Map<String, Object> include = (Map<String, Object>) body.get("include");
            Object contains = include.get("contains");
            if (contains != null && !String.valueOf(contains).isEmpty()) {
                sql.append(" AND ").append(column).append(" LIKE ?");
                args.add("%" + contains + "%");
            }
            Object eq = include.get("equal to");
            if (eq != null && !String.valueOf(eq).isEmpty()) {
                sql.append(" AND ").append(column).append(" = ?");
                args.add(eq);
            }
            Object starts = include.get("starts with");
            if (starts != null && !String.valueOf(starts).isEmpty()) {
                sql.append(" AND ").append(column).append(" LIKE ?");
                args.add(String.valueOf(starts) + "%");
            }
            Object ends = include.get("ends with");
            if (ends != null && !String.valueOf(ends).isEmpty()) {
                sql.append(" AND ").append(column).append(" LIKE ?");
                args.add("%" + String.valueOf(ends));
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 2) 公司代码搜索
    @PostMapping("/company-code")
    public Map<String, Object> companyCodeSearch(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.code AS result, c.name AS name, COALESCE(MAX(cust.city), '') AS city, COALESCE(MAX(cust.currency), '') AS currency " +
                "FROM erp_company_code c LEFT JOIN erp_customer cust ON cust.company_code = c.code WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object companyCode = body.get("companyCode");
            if (companyCode != null && !String.valueOf(companyCode).isEmpty()) {
                sql.append(" AND c.code LIKE ?"); args.add("%" + companyCode + "%");
            }
            Object city = body.get("city");
            if (city != null && !String.valueOf(city).isEmpty()) {
                sql.append(" AND cust.city LIKE ?"); args.add("%" + city + "%");
            }
            Object companyName = body.get("companyName");
            if (companyName != null && !String.valueOf(companyName).isEmpty()) {
                sql.append(" AND c.name LIKE ?"); args.add("%" + companyName + "%");
            }
            Object currency = body.get("currency");
            if (currency != null && !String.valueOf(currency).isEmpty()) {
                sql.append(" AND cust.currency = ?"); args.add(currency);
            }
        }
        sql.append(" GROUP BY c.code, c.name ORDER BY c.code");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 3) 国家搜索（来自客户表去重）
    @PostMapping("/country")
    public Map<String, Object> countrySearch(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT country AS result FROM erp_customer WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null && body.get("Country Name Search") instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) body.get("Country Name Search");
            Object name = m.get("country name");
            if (name != null && !String.valueOf(name).isEmpty()) {
                sql.append(" AND country LIKE ?"); args.add("%" + name + "%");
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 4) 客户搜索
    @PostMapping("/customer")
    public Map<String, Object> customerSearch(@RequestBody Map<String, Object> body) {
        StringBuilder sql = new StringBuilder(
                "SELECT customer_id AS result, name AS name FROM erp_customer WHERE 1=1");
        List<Object> args = new ArrayList<>();
        Object v;
        v = body.get("customer"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND CAST(customer_id AS CHAR) LIKE ?"); args.add("%" + v + "%"); }
        v = body.get("postalCode"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND postal_code LIKE ?"); args.add("%" + v + "%"); }
        v = body.get("companyCode"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND company_code = ?"); args.add(v); }
        v = body.get("city"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND city LIKE ?"); args.add("%" + v + "%"); }
        v = body.get("searchTerm"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND search_term LIKE ?"); args.add("%" + v + "%"); }
        v = body.get("customerName"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND name LIKE ?"); args.add("%" + v + "%"); }
        sql.append(" ORDER BY customer_id LIMIT 100");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 5) 业务伙伴搜索（基于客户表）
    @PostMapping("/business-partner")
    public Map<String, Object> bpSearch(@RequestBody Map<String, Object> body) {
        StringBuilder sql = new StringBuilder(
                "SELECT customer_id AS result, name AS name, company_code AS companyCode FROM erp_customer WHERE 1=1");
        List<Object> args = new ArrayList<>();
        Object block = body.get("Condition Search");
        if (block instanceof Map) {
            Map<String, Object> cm = (Map<String, Object>) block;
            Object v;
            v = cm.get("Customer"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND CAST(customer_id AS CHAR) LIKE ?"); args.add("%" + v + "%"); }
            v = cm.get("City"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND city LIKE ?"); args.add("%" + v + "%"); }
            v = cm.get("Name"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND name LIKE ?"); args.add("%" + v + "%"); }
            v = cm.get("Country Key"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND country LIKE ?"); args.add("%" + v + "%"); }
            v = cm.get("Postal Code"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND postal_code LIKE ?"); args.add("%" + v + "%"); }
        }
        Object idBlock = body.get("ID Search");
        if (idBlock instanceof Map) {
            Map<String, Object> im = (Map<String, Object>) idBlock;
            Object include = im.get("include");
            if (include instanceof Map) {
                Object eq = ((Map<?, ?>) include).get("equal to");
                if (eq != null && !String.valueOf(eq).isEmpty()) {
                    sql.append(" AND customer_id = ?"); args.add(eq);
                }
                Object contains = ((Map<?, ?>) include).get("contains");
                if (contains != null && !String.valueOf(contains).isEmpty()) {
                    sql.append(" AND CAST(customer_id AS CHAR) LIKE ?"); args.add("%" + contains + "%");
                }
            }
        }
        sql.append(" ORDER BY customer_id LIMIT 100");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 6) G/L Account（用对账科目表近似）
    @PostMapping("/gl-account")
    public Map<String, Object> glAccount(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT account_id AS glAccount, name AS shortText FROM erp_reconciliation_account WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("glAccount"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND account_id LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("shortText"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND name LIKE ?"); args.add("%" + v + "%"); }
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("glAccount"));
            m.put("chartOfAccounts", "");
            m.put("shortText", r.get("shortText"));
            m.put("companyCode", "");
            m.put("longText", "");
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 7) 货币单位 -> 使用货币表
    @PostMapping("/currency-unit")
    public Map<String, Object> currencyUnit() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT currency_code, name FROM erp_currency");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("currency_code"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 8) 工厂搜索
    @PostMapping("/plant")
    public Map<String, Object> plant(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT plant_id AS result, plant_name AS plantName, COALESCE(city,'') AS city FROM erp_plant_name WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object pn = body.get("plantName");
            if (pn != null && !String.valueOf(pn).isEmpty()) {
                sql.append(" AND plant_name LIKE ?"); args.add("%" + pn + "%");
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 9) 存储位置
    @PostMapping("/storage-location")
    public Map<String, Object> storageLocation() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT loc_id, name FROM erp_storage_location");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("loc_id"));
            m.put("description", r.get("name"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 10) 物料
    @PostMapping("/material")
    public Map<String, Object> material(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT CAST(mat_id AS CHAR) AS result, mat_desc AS matDesc FROM erp_material WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object material = body.get("material");
            if (material != null && !String.valueOf(material).isEmpty()) {
                sql.append(" AND CAST(mat_id AS CHAR) LIKE ?"); args.add("%" + material + "%");
            }
            Object md = body.get("materialDescription");
            if (md != null && !String.valueOf(md).isEmpty()) {
                sql.append(" AND mat_desc LIKE ?"); args.add("%" + md + "%");
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 11) 物料文档（以发货记录gi_id及年份近似）
    @PostMapping("/material-description")
    public Map<String, Object> materialDocument(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT gi_id AS result, YEAR(posting_date) AS materialDocumentYear FROM erp_good_issue WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null && body.get("include") instanceof Map) {
            Map<String, Object> include = (Map<String, Object>) body.get("include");
            Object eq = include.get("equal to");
            if (eq != null && !String.valueOf(eq).isEmpty()) {
                sql.append(" AND gi_id = ?"); args.add(eq);
            }
            Object contains = include.get("contains");
            if (contains != null && !String.valueOf(contains).isEmpty()) {
                sql.append(" AND CAST(gi_id AS CHAR) LIKE ?"); args.add("%" + contains + "%");
            }
        }
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 12) 报价单ID
    @PostMapping("/quotation-id")
    public Map<String, Object> quotationId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT quotation_id AS result FROM erp_quotation WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object inc = body.get("include");
            if (inc instanceof Map) {
                Object eq = ((Map<?, ?>) inc).get("equal to");
                if (eq != null && !String.valueOf(eq).isEmpty()) { sql.append(" AND quotation_id = ?"); args.add(eq); }
                Object contains = ((Map<?, ?>) inc).get("contains");
                if (contains != null && !String.valueOf(contains).isEmpty()) { sql.append(" AND CAST(quotation_id AS CHAR) LIKE ?"); args.add("%" + contains + "%"); }
                Object gt = ((Map<?, ?>) inc).get("greater than");
                if (gt != null && !String.valueOf(gt).isEmpty()) { sql.append(" AND quotation_id > ?"); args.add(gt); }
                Object lt = ((Map<?, ?>) inc).get("less than");
                if (lt != null && !String.valueOf(lt).isEmpty()) { sql.append(" AND quotation_id < ?"); args.add(lt); }
            }
        }
        sql.append(" ORDER BY quotation_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 13) 物料单位（以物料表base_uom去重近似）
    @PostMapping("/material-unit")
    public Map<String, Object> materialUnit() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT DISTINCT base_uom AS u FROM erp_material");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("u"));
            m.put("description", r.get("u"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }


    // 14) 关系类型（去重）
    @PostMapping("/relation")
    public Map<String, Object> relation() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT DISTINCT rel_category FROM erp_relation");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("rel_category"));
            m.put("direction", "BP1->BP2");
            m.put("desription", r.get("rel_category"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 15) 询价类型
    @PostMapping("/inquiry-type")
    public Map<String, Object> inquiryType() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT DISTINCT inquiry_type, COALESCE(inquiry_type,'') AS description FROM erp_inquiry");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("inquiry_type"));
            m.put("description", r.get("description"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 16) 销售组织
    @PostMapping("/sales-org")
    public Map<String, Object> salesOrg() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT org_id, name FROM erp_sales_org");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("org_id"));
            m.put("description", r.get("name"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 17) 分销渠道
    @PostMapping("/distribution-channel")
    public Map<String, Object> distributionChannel() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT channel_id, name FROM erp_distribution_channel");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("channel_id"));
            m.put("dchl", r.get("channel_id"));
            m.put("description", r.get("name"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 18) 部门（产品线）
    @PostMapping("/division")
    public Map<String, Object> division() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT division_id, name FROM erp_division");
        List<Map<String, Object>> list = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("result", r.get("division_id"));
            m.put("dchl", "");
            m.put("dv", r.get("division_id"));
            m.put("description", r.get("name"));
            return m;
        }).collect(Collectors.toList());
        return ok(list);
    }

    // 19) 售达方
    @PostMapping("/sold-to-party")
    public Map<String, Object> soldToParty(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT customer_id AS result, city, country FROM erp_customer WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("customer"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND CAST(customer_id AS CHAR) LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("city"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND city LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("name"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND name LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("countryKey"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND country LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("postalCode"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND postal_code LIKE ?"); args.add("%" + v + "%"); }
        }
        sql.append(" ORDER BY customer_id LIMIT 100");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }


    // 20) 销售订单ID
    @PostMapping("/salesOrder-id")
    public Map<String, Object> salesOrderId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT so_id AS result, customer_no AS soldToParty, COALESCE(net_value,0) AS netValue, '' AS customerReference FROM erp_sales_order_hdr WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("quotation_id"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND quote_id = ?"); args.add(v); }
            v = body.get("soldToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND customer_no = ?"); args.add(v); }
            v = body.get("shipToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND contact_id = ?"); args.add(v); }
            v = body.get("search_term"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND CAST(so_id AS CHAR) LIKE ?"); args.add("%" + v + "%"); }
        }
        sql.append(" ORDER BY so_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 21) 询价单ID
    @PostMapping("/inquiry-id")
    public Map<String, Object> inquiryId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT inquiry_id AS result, cust_ref AS purchaseOrderNumber, sold_tp AS soldToParty, ship_tp AS shipToParty, cust_ref AS customerRef, customer_reference_date AS customerRefDate FROM erp_inquiry WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("purchaseOrderNumber"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND cust_ref LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("soldToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND sold_tp = ?"); args.add(v); }
            v = body.get("shipToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND ship_tp = ?"); args.add(v); }
            v = body.get("customerRef"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND cust_ref LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("customerRefDate"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND customer_reference_date = ?"); args.add(v); }
            Object cm = body.get("containMaterials");
            if (cm instanceof List<?> list && !list.isEmpty()) {
                sql.append(" AND inquiry_id IN (");
                sql.append("SELECT inquiry_id FROM erp_inquiry_item WHERE ");
                boolean first = true;
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m && m.get("id") != null) {
                        sql.append(first ? "" : " OR ");
                        sql.append("mat_id = ?");
                        args.add(m.get("id"));
                        first = false;
                    }
                }
                sql.append(")");
            }
        }
        sql.append(" ORDER BY inquiry_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 22) 交货单ID
    @PostMapping("/delivery-id")
    public Map<String, Object> deliveryId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT dlv_id AS result, '' AS description, shipping_point AS shippingPoint, ship_tp AS shipToParty, pick_date_plan AS pickingDate, NULL AS loadingDate, gi_date AS plannedGIDate, NULL AS deliveryDate, status AS pickingStatus FROM erp_outbound_delivery WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("shippingPoint"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND shipping_point LIKE ?"); args.add("%" + v + "%"); }
            v = body.get("shipToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND ship_tp = ?"); args.add(v); }
            // pickingStatus OR 条件
            Object ps = body.get("pickingStatus");
            if (ps instanceof List<?> list && !list.isEmpty()) {
                sql.append(" AND (");
                boolean first = true;
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m && m.get("status") != null) {
                        sql.append(first ? "" : " OR ");
                        sql.append("status = ?");
                        args.add(m.get("status"));
                        first = false;
                    }
                }
                sql.append(")");
            }
        }
        sql.append(" ORDER BY dlv_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 23) 开票凭证
    @PostMapping("/billing-document-id")
    public Map<String, Object> billingDocumentId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT b.bill_id AS result, b.customer_id AS soldToParty, b.net AS netValue, b.billing_date AS billingDate, COALESCE(s.currency,'') AS currency FROM erp_billing_hdr b LEFT JOIN erp_outbound_delivery d ON d.dlv_id = b.dlv_id LEFT JOIN erp_sales_order_hdr s ON s.so_id = d.so_id WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("soldToParty"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND b.customer_id = ?"); args.add(v); }
            v = body.get("billingDate"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND b.billing_date = ?"); args.add(v); }
        }
        sql.append(" ORDER BY b.bill_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

    // 24) 关系ID
    @PostMapping("/relation-id")
    public Map<String, Object> relationId(@RequestBody(required = false) Map<String, Object> body) {
        StringBuilder sql = new StringBuilder("SELECT relation_id AS result, bp1 AS BP1, bp2 AS BP2, valid_from AS validFrom, valid_to AS validTo, rel_category AS relation FROM erp_relation WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (body != null) {
            Object v;
            v = body.get("BP1"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND bp1 = ?"); args.add(v); }
            v = body.get("BP2"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND bp2 = ?"); args.add(v); }
            v = body.get("validFrom"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND valid_from >= ?"); args.add(v); }
            v = body.get("validTo"); if (v != null && !String.valueOf(v).isEmpty()) { sql.append(" AND valid_to <= ?"); args.add(v); }
            Object cc = body.get("containCategory");
            if (cc instanceof List<?> list && !list.isEmpty()) {
                sql.append(" AND (");
                boolean first = true;
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m && m.get("relation") != null) {
                        sql.append(first ? "" : " OR ");
                        sql.append("rel_category = ?");
                        args.add(m.get("relation"));
                        first = false;
                    }
                }
                sql.append(")");
            }
        }
        sql.append(" ORDER BY relation_id DESC LIMIT 200");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ok(list);
    }

}

