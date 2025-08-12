package webserver.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webserver.common.Response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository repo;

    public Response<DashboardDTO.Overview> getOverview(String period) {
        LocalDate today = LocalDate.now();
        LocalDate start = switch (period == null ? "today" : period) {
            case "week" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            default -> today; // today
        };
        DashboardDTO.Overview overview = new DashboardDTO.Overview(
                repo.countNewOrders(start),
                repo.sumOrderNetValue(start),
                repo.countShipments(start),
                period,
                start,
                today
        );
        return Response.success(overview);
    }

    public Response<List<DashboardDTO.CustomerContribution>> getTopCustomers() {
        // 最近5个工作日的开始日期（跳过周末）
        LocalDate today = LocalDate.now();
        int added = 0;
        LocalDate cursor = today;
        while (added < 5) {
            if (!(cursor.getDayOfWeek() == DayOfWeek.SATURDAY || cursor.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                added++;
            }
            cursor = cursor.minusDays(1);
        }
        LocalDate start = cursor.plusDays(1); // 最后一次减一天后再加回来
        return Response.success(repo.topCustomers(start));
    }

    public Response<List<DashboardDTO.MaterialStat>> getMaterials(String type) {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if ("bottom".equalsIgnoreCase(type)) {
            return Response.success(repo.bottomMaterials(start, 5));
        }
        return Response.success(repo.topMaterials(start, 5));
    }

    public Response<List<DashboardDTO.UrgentOrder>> getUrgentOrders() {
        return Response.success(repo.urgentOrders(3));
    }

    public Response<DashboardDTO.FinancialRisk> getFinancialRisk() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        double overdue = repo.calcFinancialRisk(monthStart, today);
        return Response.success(new DashboardDTO.FinancialRisk(overdue, monthStart, today));
    }

    public Response<DashboardDTO.RevenueComparison> getRevenueComparison() {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);
        LocalDate lastWeekStart = lastWeekEnd.minusDays(6);

        double lastWeek = repo.sumPaymentsBetween(lastWeekStart, lastWeekEnd);
        double thisWeek = repo.sumPaymentsBetween(thisWeekStart, today);

        DashboardDTO.RevenueComparison dto = new DashboardDTO.RevenueComparison(
                lastWeek, thisWeek,
                lastWeekStart, lastWeekEnd,
                thisWeekStart, today
        );
        return Response.success(dto);
    }

    // 可选：一次性获取所有卡片数据（便于前端减少请求数）
    public Response<DashboardDTO.AllCards> getAll(String period) {
        DashboardDTO.AllCards all = new DashboardDTO.AllCards(
                getOverview(period).getData(),
                getTopCustomers().getData(),
                getMaterials("top").getData(),
                getMaterials("bottom").getData(),
                getUrgentOrders().getData(),
                getFinancialRisk().getData(),
                getRevenueComparison().getData()
        );
        return Response.success(all);
    }
}

