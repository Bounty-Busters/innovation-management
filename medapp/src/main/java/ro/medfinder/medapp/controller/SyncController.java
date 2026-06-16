package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.medfinder.medapp.config.CustomUserDetails;
import ro.medfinder.medapp.dto.SyncRequestForm;
import ro.medfinder.medapp.entity.Pharmacy;
import ro.medfinder.medapp.entity.Pharmacist;
import ro.medfinder.medapp.entity.SyncLog;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.service.PharmacyService;
import ro.medfinder.medapp.service.SyncService;

import java.util.List;

@Controller
@RequestMapping("/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final PharmacyService pharmacyService;

    @GetMapping
    public String syncPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Pharmacy> pharmacies;
        if (userDetails.getUser().getRole() == Role.PHARMACIST) {
            pharmacies = List.of(((Pharmacist) userDetails.getUser()).getLocation().getPharmacy());
        } else {
            pharmacies = pharmacyService.getPharmaciesForUser(userDetails.getUser(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        }

        model.addAttribute("pharmacies", pharmacies);
        model.addAttribute("syncForm", new SyncRequestForm());
        return "admin/sync/manage";
    }

    @PostMapping("/trigger")
    public String triggerSync(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @ModelAttribute SyncRequestForm form,
                              RedirectAttributes redirectAttributes) {
        try {
            syncService.triggerSync(form, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Synchronization completed. Check the logs for details.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error triggering sync: " + e.getMessage());
        }
        return "redirect:/admin/sync/logs";
    }

    @GetMapping("/logs")
    public String logsPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<SyncLog> logs = syncService.getLogsForUser(userDetails.getUser(), pageable);
        model.addAttribute("logPage", logs);
        return "admin/sync/logs";
    }
}
