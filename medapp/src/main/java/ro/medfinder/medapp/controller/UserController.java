package ro.medfinder.medapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.UserForm;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.OrderRepository;
import ro.medfinder.medapp.service.LocationService;
import ro.medfinder.medapp.service.PharmacyService;
import ro.medfinder.medapp.service.UserService;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PharmacyService pharmacyService;
    private final LocationService locationService;
    private final OrderRepository orderRepository;

    @GetMapping("/clients")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String listClients(@org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable, Model model) {
        model.addAttribute("clientPage", userService.getAllClients(pageable));
        return "admin/users/clients";
    }

    @GetMapping("/pharm-owners")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String listPharmOwners(@org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable, Model model) {
        model.addAttribute("ownerPage", userService.getAllPharmOwners(pageable));
        return "admin/users/pharm-owners";
    }

    @GetMapping("/pharmacists")
    public String listPharmacists(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable,
                                  Model model) {
        model.addAttribute("pharmacistPage", userService.getPharmacistsForUser(userDetails.getUser(), pageable));
        return "admin/users/pharmacists";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.getUserById(id);

        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setEnabled(user.getEnabled());
        form.setRole(user.getRole().name());

        if (user instanceof PharmOwner pharmOwner) {
            form.setCompanyName(pharmOwner.getCompanyName());
        }
        if (user instanceof Pharmacist pharmacist && pharmacist.getLocation() != null) {
            form.setLocationId(pharmacist.getLocation().getId());
        }

        model.addAttribute("userForm", form);
        addFormOptions(model, userDetails.getUser(), user.getRole());
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid @ModelAttribute("userForm") UserForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.getUserById(id);
            addFormOptions(model, userDetails.getUser(), user.getRole());
            return "admin/users/form";
        }
        try {
            userService.updateUser(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return redirectToUserList(form.getRole());
        } catch (IllegalArgumentException e) {
            User user = userService.getUserById(id);
            model.addAttribute("errorMessage", e.getMessage());
            addFormOptions(model, userDetails.getUser(), user.getRole());
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleEnabled(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        userService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "User " + (user.getEnabled() ? "disabled" : "enabled") + " successfully");
        return redirectToUserList(user.getRole().name());
    }

    private String redirectToUserList(String role) {
        return switch (role) {
            case "CLIENT" -> "redirect:/admin/users/clients";
            case "PHARM_OWNER" -> "redirect:/admin/users/pharm-owners";
            case "PHARMACIST" -> "redirect:/admin/users/pharmacists";
            default -> "redirect:/admin/dashboard";
        };
    }

    private void addFormOptions(Model model, User currentUser, Role userRole) {
        if (userRole == Role.PHARMACIST) {
            model.addAttribute("locations", locationService.getLocationsForUser(currentUser, org.springframework.data.domain.Pageable.unpaged()).getContent());
        }
    }
}
