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
import ro.medfinder.medapp.dto.LocationForm;
import ro.medfinder.medapp.entity.Location;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.MedStockRepository;
import ro.medfinder.medapp.repository.PharmacistRepository;
import ro.medfinder.medapp.service.LocationService;
import ro.medfinder.medapp.service.PharmacyService;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final PharmacyService pharmacyService;
    private final PharmacistRepository pharmacistRepository;
    private final MedStockRepository medStockRepository;

    @GetMapping
    public String listLocations(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable,
                                Model model) {
        model.addAttribute("locationPage", locationService.getLocationsForUser(userDetails.getUser(), pageable));
        return "admin/locations/list";
    }

    @GetMapping("/new")
    public String newLocationForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        LocationForm form = new LocationForm();
        form.setWorkingHours(createEmptyWorkingHours());
        model.addAttribute("locationForm", form);
        addPharmacyOptions(model, userDetails.getUser());
        return "admin/locations/form";
    }

    @PostMapping
    public String createLocation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("locationForm") LocationForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            addPharmacyOptions(model, userDetails.getUser());
            return "admin/locations/form";
        }
        try {
            locationService.createLocation(form, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Location created successfully");
            return "redirect:/admin/locations";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            addPharmacyOptions(model, userDetails.getUser());
            return "admin/locations/form";
        }
    }

    @GetMapping("/{id}")
    public String viewLocation(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        model.addAttribute("workingHours", locationService.getWorkingHoursForLocation(id));
        model.addAttribute("pharmacists", pharmacistRepository.findByLocationId(id));
        model.addAttribute("medStocks", medStockRepository.findByLocationId(id));
        return "admin/locations/view";
    }

    @GetMapping("/{id}/edit")
    public String editLocationForm(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Location location = locationService.getLocationById(id);

        LocationForm form = new LocationForm();
        form.setId(location.getId());
        form.setName(location.getName());
        form.setAddress(location.getAddress());
        form.setCity(location.getCity());
        form.setCounty(location.getCounty());
        form.setPostalCode(location.getPostalCode());
        form.setLatitude(location.getLatitude());
        form.setLongitude(location.getLongitude());
        form.setPhone(location.getPhone());
        form.setActive(location.getActive());
        form.setPharmacyId(location.getPharmacy().getId());

        // Build working hours from existing data
        var existingHours = locationService.getWorkingHoursForLocation(id);
        List<LocationForm.WorkingHourEntry> entries = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            LocationForm.WorkingHourEntry entry = new LocationForm.WorkingHourEntry();
            entry.setDayOfWeek(day.name());
            existingHours.stream()
                    .filter(wh -> wh.getDayOfWeek() == day)
                    .findFirst()
                    .ifPresent(wh -> {
                        entry.setClosed(wh.getClosed());
                        entry.setOpenTime(wh.getOpenTime() != null ? wh.getOpenTime().toString() : "");
                        entry.setCloseTime(wh.getCloseTime() != null ? wh.getCloseTime().toString() : "");
                    });
            entries.add(entry);
        }
        form.setWorkingHours(entries);

        model.addAttribute("locationForm", form);
        addPharmacyOptions(model, userDetails.getUser());
        return "admin/locations/form";
    }

    @PostMapping("/{id}")
    public String updateLocation(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("locationForm") LocationForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            addPharmacyOptions(model, userDetails.getUser());
            return "admin/locations/form";
        }
        try {
            locationService.updateLocation(id, form, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Location updated successfully");
            return "redirect:/admin/locations";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            addPharmacyOptions(model, userDetails.getUser());
            return "admin/locations/form";
        }
    }

    private void addPharmacyOptions(Model model, User user) {
        if (user.getRole() == Role.SUPER_USER || user.getRole() == Role.PHARM_OWNER) {
            model.addAttribute("pharmacies", pharmacyService.getPharmaciesForUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent());
        }
    }

    private List<LocationForm.WorkingHourEntry> createEmptyWorkingHours() {
        List<LocationForm.WorkingHourEntry> entries = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            LocationForm.WorkingHourEntry entry = new LocationForm.WorkingHourEntry();
            entry.setDayOfWeek(day.name());
            entry.setOpenTime("08:00");
            entry.setCloseTime("20:00");
            entry.setClosed(day == DayOfWeek.SUNDAY);
            entries.add(entry);
        }
        return entries;
    }
}
