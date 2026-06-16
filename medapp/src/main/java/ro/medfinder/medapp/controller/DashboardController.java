package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.DashboardChartData;
import ro.medfinder.medapp.dto.DashboardStats;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.service.DashboardService;
import ro.medfinder.medapp.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // Stats for the top cards
        DashboardStats stats = dashboardService.getStatsForUser(userDetails.getUser());
        model.addAttribute("stats", stats);
        
        // Data for Chart.js
        DashboardChartData chartData = dashboardService.getChartDataForUser(userDetails.getUser());
        model.addAttribute("chartData", chartData);
        
        // Recent activity table (last 10 orders)
        Page<Order> recentOrdersPage = orderService.getOrdersForUser(userDetails.getUser(), null, PageRequest.of(0, 10));
        model.addAttribute("recentOrders", recentOrdersPage.getContent());
        
        return "admin/dashboard";
    }
}
