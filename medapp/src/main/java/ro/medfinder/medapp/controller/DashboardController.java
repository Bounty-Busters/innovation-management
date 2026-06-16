package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.DashboardStats;
import ro.medfinder.medapp.service.DashboardService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        DashboardStats stats = dashboardService.getStatsForUser(userDetails.getUser());
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }
}
