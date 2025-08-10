package webserver.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService service;

    // 核心业绩总览
    @GetMapping("/overview")
    public Response<DashboardDTO.Overview> overview(@RequestParam(defaultValue = "today") String period) {
        return service.getOverview(period);
    }

    // Top 3 贡献客户
    @GetMapping("/top-customers")
    public Response<?> topCustomers() {
        return service.getTopCustomers();
    }

    // 畅销/滞销物料
    @GetMapping("/materials")
    public Response<?> materials(@RequestParam(defaultValue = "top") String type) {
        return service.getMaterials(type);
    }

    // 紧急交付预警
    @GetMapping("/urgent-orders")
    public Response<?> urgentOrders() {
        return service.getUrgentOrders();
    }

    // 财务风险预警
    @GetMapping("/financial-risk")
    public Response<?> financialRisk() {
        return service.getFinancialRisk();
    }

    // 营收对比
    @GetMapping("/revenue-comparison")
    public Response<?> revenueComparison() {
        return service.getRevenueComparison();
    }

    // 可选：一次性获取全部
    @GetMapping("/all")
    public Response<?> all(@RequestParam(defaultValue = "today") String period) {
        return service.getAll(period);
    }
}

