package ro.medfinder.medapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.PharmacyForm;
import ro.medfinder.medapp.entity.Pharmacy;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.LocationRepository;
import ro.medfinder.medapp.repository.PharmacistRepository;
import ro.medfinder.medapp.repository.PharmOwnerRepository;
import ro.medfinder.medapp.service.PharmacyService;

@Controller
@RequestMapping("/admin/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final PharmOwnerRepository pharmOwnerRepository;
    private final LocationRepository locationRepository;
    private final PharmacistRepository pharmacistRepository;

    @GetMapping
    public String listPharmacies(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable,
                                 Model model) {
        User user = userDetails.getUser();
        model.addAttribute("pharmacyPage", pharmacyService.getPharmaciesForUser(user, pageable));
        return "admin/pharmacies/list";
    }

    @GetMapping("/new")
    public String newPharmacyForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("pharmacyForm", new PharmacyForm());
        addOwnerOptions(model, userDetails.getUser());
        return "admin/pharmacies/form";
    }

    @PostMapping
    public String createPharmacy(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("pharmacyForm") PharmacyForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            addOwnerOptions(model, userDetails.getUser());
            return "admin/pharmacies/form";
        }
        try {
            pharmacyService.createPharmacy(form, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy created successfully");
            return "redirect:/admin/pharmacies";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            addOwnerOptions(model, userDetails.getUser());
            return "admin/pharmacies/form";
        }
    }

    @GetMapping("/{id}")
    public String viewPharmacy(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Pharmacy pharmacy = pharmacyService.getPharmacyById(id);
        model.addAttribute("pharmacy", pharmacy);
        model.addAttribute("locations", locationRepository.findByPharmacyId(id));
        model.addAttribute("pharmacists", pharmacistRepository.findByLocationPharmacyOwnerId(pharmacy.getOwner().getId()));
        return "admin/pharmacies/view";
    }

    @GetMapping("/{id}/edit")
    public String editPharmacyForm(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Pharmacy pharmacy = pharmacyService.getPharmacyById(id);

        PharmacyForm form = new PharmacyForm();
        form.setId(pharmacy.getId());
        form.setName(pharmacy.getName());
        form.setCui(pharmacy.getCui());
        form.setPhone(pharmacy.getPhone());
        form.setEmail(pharmacy.getEmail());
        form.setWebsite(pharmacy.getWebsite());
        form.setLogoUrl(pharmacy.getLogoUrl());
        form.setSyncEnabled(pharmacy.getSyncEnabled());
        form.setSyncEndpointUrl(pharmacy.getSyncEndpointUrl());
        if (pharmacy.getOwner() != null) {
            form.setOwnerId(pharmacy.getOwner().getId());
        }

        model.addAttribute("pharmacyForm", form);
        addOwnerOptions(model, userDetails.getUser());
        return "admin/pharmacies/form";
    }

    @PostMapping("/{id}")
    public String updatePharmacy(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("pharmacyForm") PharmacyForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            addOwnerOptions(model, userDetails.getUser());
            return "admin/pharmacies/form";
        }
        try {
            pharmacyService.updatePharmacy(id, form, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy updated successfully");
            return "redirect:/admin/pharmacies";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            addOwnerOptions(model, userDetails.getUser());
            return "admin/pharmacies/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        pharmacyService.toggleActive(id, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Pharmacy status updated");
        return "redirect:/admin/pharmacies";
    }

    private void addOwnerOptions(Model model, User user) {
        if (user.getRole() == Role.SUPER_USER) {
            model.addAttribute("owners", pharmOwnerRepository.findAll());
        }
    }
}
