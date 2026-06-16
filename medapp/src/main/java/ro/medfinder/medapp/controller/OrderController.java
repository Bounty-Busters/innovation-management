package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.OrderStatusUpdate;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(value = "status", required = false) OrderStatus statusFilter,
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {
        Page<Order> orderPage = orderService.getOrdersForUser(userDetails.getUser(), statusFilter, pageable);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/orders/view";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @ModelAttribute OrderStatusUpdate update,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, update);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + update.getNewStatus());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
