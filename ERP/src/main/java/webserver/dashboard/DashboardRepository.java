package webserver.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import webserver.mapper.DashboardMapper;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {
    private final DashboardMapper mapper;

    public int countNewOrders(LocalDate start) {
        Integer v = mapper.countNewOrders(start);
        return v == null ? 0 : v;
    }

    public double sumOrderNetValue(LocalDate start) {
        Double v = mapper.sumOrderNetValue(start);
        return v == null ? 0.0 : v;
    }

    public int countShipments(LocalDate start) {
        Integer v = mapper.countShipments(start);
        return v == null ? 0 : v;
    }

    public List<DashboardDTO.CustomerContribution> topCustomers(LocalDate start) {
        return mapper.topCustomers(start);
    }

    public List<DashboardDTO.MaterialStat> topMaterials(LocalDate start, int limit) {
        return mapper.topMaterials(start, limit);
    }

    public List<DashboardDTO.MaterialStat> bottomMaterials(LocalDate start, int limit) {
        return mapper.bottomMaterials(start, limit);
    }

    public List<DashboardDTO.UrgentOrder> urgentOrders(int limit) {
        return mapper.urgentOrders(limit);
    }

    public double calcFinancialRisk(LocalDate monthStart, LocalDate today) {
        Double v = mapper.calcFinancialRisk(monthStart, today);
        return v == null ? 0.0 : v;
    }

    public double sumPaymentsBetween(LocalDate start, LocalDate end) {
        Double v = mapper.sumPaymentsBetween(start, end);
        return v == null ? 0.0 : v;
    }
}

