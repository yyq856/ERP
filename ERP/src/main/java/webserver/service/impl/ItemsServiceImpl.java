package webserver.service.impl;

import org.springframework.stereotype.Service;
import webserver.common.Response;
import webserver.mapper.MaterialMapper;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.Material;
import webserver.pojo.ValidateItemsResponse;
import webserver.service.ItemsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemsServiceImpl implements ItemsService {

    private final MaterialMapper materialMapper;

    public ItemsServiceImpl(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public Response<ValidateItemsResponse> itemsTabQuery(List<ItemsTabQueryRequest.ItemInput> items) {
        List<Integer> badIndices = new ArrayList<>();
        List<ValidateItemsResponse.PricingElementBreakdown> emptyPE = new ArrayList<>();
        List<ValidateItemsResponse.ItemBreakdown> breakdowns = new ArrayList<>();

        double totalNet = 0.0;
        String totalCurr = null;

        for (int i = 0; i < items.size(); i++) {
            ItemsTabQueryRequest.ItemInput in = items.get(i);

            // 物料必须存在才能补全，否则标为bad并尽力回传
            Long matId = parseLong(in.getMaterial());
            Material mat = (matId != null ? materialMapper.findById(matId) : null);
            if (mat == null) {
                badIndices.add(i);
            }

            // item 行号：若为空则按顺序生成 10,20,...
            String itemNo = trimToNull(in.getItem());
            if (itemNo == null) {
                itemNo = String.valueOf((i + 1) * 10);
            }

            // 数量与单位
            String orderQtyStr = trimToNull(in.getOrderQuantity());
            double orderQty = parseDouble(orderQtyStr, 0.0);

            String uom = trimToNull(in.getOrderQuantityUnit());
            if (uom == null && mat != null) {
                uom = mat.getBaseUom();
            }

            // 描述
            String desc = trimToNull(in.getDescription());
            if (desc == null && mat != null) {
                desc = mat.getMatDesc();
            }

            // 要求交货日期 默认今天
            String reqDate = trimToNull(in.getReqDelivDate());
            if (reqDate == null) {
                reqDate = LocalDate.now().toString();
            }

            // 定价日期（可留空，若需要可同今天）
            String pricingDate = trimToNull(in.getPricingDate());

            // 订单概率 默认100
            String orderProb = trimToNull(in.getOrderProbability());
            if (orderProb == null) {
                orderProb = "100";
            }

            // 货币：简单策略，沿用 material 未提供币种时用 CNY
            String curr = "CNY";

            // 计算基础定价：若未提供 pricingElements 或 material 补全场景，仅生成基础定价
            List<ValidateItemsResponse.PricingElementBreakdown> peList = new ArrayList<>();
            double basePricePerUnit = (mat != null && mat.getSrdPrice() != null) ? mat.getSrdPrice() : 0.0;
            double baseAmount = round2(basePricePerUnit * orderQty);

            boolean hasAnyInputPE = in.getPricingElements() != null && !in.getPricingElements().isEmpty();

            if (!hasAnyInputPE) {
                peList.add(buildBasePricePE(basePricePerUnit, uom, curr));
            } else {
                // 拷贝输入的 pricingElements，尝试补全必要字段
                for (ItemsTabQueryRequest.PricingElementInput peIn : in.getPricingElements()) {
                    ValidateItemsResponse.PricingElementBreakdown pe = new ValidateItemsResponse.PricingElementBreakdown();
                    pe.setCnty(nvl(peIn.getCnty(), "BASE"));
                    pe.setName(nvl(peIn.getName(), "Base Price"));
                    pe.setAmount(nvl(peIn.getAmount(), String.valueOf(basePricePerUnit))); // 若未给金额，默认用基础单价
                    String amountStrIn = trimToNull(peIn.getAmount());
                    boolean pctIn = amountStrIn != null && amountStrIn.endsWith("%");
                    pe.setCity(nvl(peIn.getCity(), pctIn ? "%" : curr));
                    pe.setPer(nvl(peIn.getPer(), "1"));
                    pe.setUom(nvl(peIn.getUom(), uom));
                    // conditionValue 稍后统一计算
                    pe.setCurr(nvl(peIn.getCurr(), curr));
                    pe.setStatus(peIn.getStatus());
                    pe.setNumC(peIn.getNumC());
                    pe.setAtoMtsComponent(peIn.getAtoMtsComponent());
                    pe.setOun(peIn.getOun());
                    pe.setCconDe(peIn.getCconDe());
                    pe.setUn(peIn.getUn());
                    pe.setConditionValue2(peIn.getConditionValue2());
                    pe.setCdCur(peIn.getCdCur());
                    pe.setStat(peIn.getStat() == null ? Boolean.TRUE : peIn.getStat());
                    peList.add(pe);
                }
                // 若只有国家代码 cnty 的新元素，需要尝试生成新元素内容（简单默认基础定价）
                for (ValidateItemsResponse.PricingElementBreakdown pe : peList) {
                    if (isOnlyCnty(pe)) {
                        pe.setName("Base Price");
                        pe.setAmount(String.valueOf(basePricePerUnit));
                        pe.setCity(curr);
                        pe.setPer("1");
                        pe.setUom(uom);
                        pe.setCurr(curr);
                        pe.setStat(Boolean.TRUE);
                    }
                }
            }

            // 根据 pricingElements 计算每行 conditionValue 与净值
            double lineNet = 0.0;
            for (ValidateItemsResponse.PricingElementBreakdown pe : peList) {
                double per = parseDouble(pe.getPer(), 1.0);
                String amountStr = trimToNull(pe.getAmount());
                double amount;
                boolean isPercent = amountStr != null && amountStr.endsWith("%");
                if (isPercent) {
                    double pct = parseDouble(amountStr.substring(0, amountStr.length() - 1), 0.0);
                    amount = round2(basePricePerUnit * orderQty * pct / 100.0);
                } else {
                    amount = parseDouble(amountStr, basePricePerUnit) * orderQty / per;
                }
                // 基础规则：BASE为正向加价，其他如折扣可能需要负值；这里简单策略：若 name 或 cnty 包含"DISC"则取负
                if (containsIgnoreCase(pe.getName(), "disc") || containsIgnoreCase(pe.getCnty(), "disc")) {
                    amount = -Math.abs(amount);
                }
                pe.setConditionValue(String.valueOf(round2(amount)));
                lineNet += amount;
            }

            lineNet = round2(lineNet);

            double tax = round2(lineNet * 0.13);

            // 回填 breakdown
            ValidateItemsResponse.ItemBreakdown br = new ValidateItemsResponse.ItemBreakdown();
            br.setItem(itemNo);
            br.setMaterial(in.getMaterial());
            br.setOrderQuantity(orderQtyStr == null ? String.valueOf(orderQty) : orderQtyStr);
            br.setOrderQuantityUnit(uom);
            br.setDescription(desc);
            br.setReqDelivDate(reqDate);
            br.setNetValue(lineNet);
            br.setNetValueUnit(curr);
            br.setTaxValue(tax);
            br.setTaxValueUnit(curr);
            br.setPricingDate(pricingDate);
            br.setOrderProbability(orderProb);
            br.setPricingElements(peList);

            breakdowns.add(br);

            totalNet += lineNet;
            if (totalCurr == null) totalCurr = curr;
        }

        ValidateItemsResponse.ValidationResult result = new ValidateItemsResponse.ValidationResult();
        result.setAllDataLegal(badIndices.isEmpty() ? 1 : 0);
        result.setBadRecordIndices(badIndices);

        ValidateItemsResponse.GeneralData general = new ValidateItemsResponse.GeneralData();
        general.setNetValue(String.valueOf(round2(totalNet)));
        general.setNetValueUnit(totalCurr == null ? "CNY" : totalCurr);
        general.setExpectOralVal(null);
        general.setExpectOralValUnit(null);

        ValidateItemsResponse.ValidateItemsData data = new ValidateItemsResponse.ValidateItemsData();
        data.setResult(result);
        data.setGeneralData(general);
        data.setBreakdowns(breakdowns);

        ValidateItemsResponse resp = new ValidateItemsResponse();
        resp.setSuccess(true);
        resp.setMessage("批量验证完成");
        resp.setData(data);

        return Response.success(resp);
    }

    private ValidateItemsResponse.PricingElementBreakdown buildBasePricePE(double basePricePerUnit, String uom, String curr) {
        ValidateItemsResponse.PricingElementBreakdown pe = new ValidateItemsResponse.PricingElementBreakdown();
        pe.setCnty("BASE");
        pe.setName("Base Price");
        pe.setAmount(String.valueOf(basePricePerUnit));
        pe.setCity(curr);
        pe.setPer("1");
        pe.setUom(uom);
        pe.setCurr(curr);
        pe.setStat(Boolean.TRUE);
        return pe;
    }

    private boolean isOnlyCnty(ValidateItemsResponse.PricingElementBreakdown pe) {
        return notNull(pe.getCnty()) &&
               isNull(pe.getName()) && isNull(pe.getAmount()) && isNull(pe.getCity()) &&
               isNull(pe.getPer()) && isNull(pe.getUom()) && isNull(pe.getCurr());
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String nvl(String v, String dv) { return trimToNull(v) == null ? dv : v; }

    private Long parseLong(String s) {
        try { return s == null ? null : Long.valueOf(s.trim()); } catch (Exception e) { return null; }
    }
    private double parseDouble(String s, double dv) {
        try { return s == null ? dv : Double.parseDouble(s.trim()); } catch (Exception e) { return dv; }
    }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    private boolean isNull(String s){ return s == null || s.trim().isEmpty(); }
    private boolean notNull(String s){ return !isNull(s); }
    private boolean containsIgnoreCase(String a, String b){
        if (a == null || b == null) return false;
        return a.toLowerCase().contains(b.toLowerCase());
    }
}

