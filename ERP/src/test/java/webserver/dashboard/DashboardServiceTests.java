package webserver.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import webserver.common.Response;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DashboardServiceTests {

    @Autowired
    private DashboardService service;

    @Test
    void overviewToday() {
        Response<DashboardDTO.Overview> res = service.getOverview("today");
        assertEquals(200, res.getCode());
        assertNotNull(res.getData());
    }
}

