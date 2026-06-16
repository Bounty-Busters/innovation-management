package ro.medfinder.medapp.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Adds currentUser and currentRole to every Thymeleaf template model.
 * This allows role-based rendering in templates with th:if="${currentRole == '...'}"
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addUserAttributes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletRequest request) {
        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
            model.addAttribute("currentRole", userDetails.getUser().getRole().name());
        }
        model.addAttribute("requestURI", request.getRequestURI());
    }
}
