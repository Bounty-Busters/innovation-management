package ro.medfinder.medapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.medfinder.medapp.dto.MedicationForm;
import ro.medfinder.medapp.entity.Medication;
import ro.medfinder.medapp.entity.enums.MedForm;
import ro.medfinder.medapp.service.MedicationService;

@Controller
@RequestMapping("/admin/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public String listMedications(@RequestParam(value = "search", required = false) String search,
                                  @PageableDefault(size = 20) Pageable pageable,
                                  Model model) {
        Page<Medication> medicationPage = medicationService.getAllMedications(search, pageable);
        model.addAttribute("medicationPage", medicationPage);
        model.addAttribute("search", search);
        return "admin/medications/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String newMedicationForm(Model model) {
        model.addAttribute("medicationForm", new MedicationForm());
        model.addAttribute("medForms", MedForm.values());
        return "admin/medications/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String createMedication(@Valid @ModelAttribute("medicationForm") MedicationForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("medForms", MedForm.values());
            return "admin/medications/form";
        }

        try {
            medicationService.createMedication(form);
            redirectAttributes.addFlashAttribute("successMessage", "Medication created successfully");
            return "redirect:/admin/medications";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("medForms", MedForm.values());
            return "admin/medications/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String editMedicationForm(@PathVariable Long id, Model model) {
        Medication medication = medicationService.getMedicationById(id);
        
        MedicationForm form = new MedicationForm();
        form.setId(medication.getId());
        form.setEan(medication.getEan());
        form.setName(medication.getName());
        form.setActiveSubstance(medication.getActiveSubstance());
        form.setForm(medication.getForm());
        form.setDosage(medication.getDosage());
        form.setCategory(medication.getCategory());
        form.setPrescriptionRequired(medication.getPrescriptionRequired());
        form.setDescription(medication.getDescription());
        
        model.addAttribute("medicationForm", form);
        model.addAttribute("medForms", MedForm.values());
        return "admin/medications/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String updateMedication(@PathVariable Long id,
                                   @Valid @ModelAttribute("medicationForm") MedicationForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("medForms", MedForm.values());
            return "admin/medications/form";
        }

        try {
            medicationService.updateMedication(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Medication updated successfully");
            return "redirect:/admin/medications";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("medForms", MedForm.values());
            return "admin/medications/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('SUPER_USER')")
    public String deleteMedication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        medicationService.deleteMedication(id);
        redirectAttributes.addFlashAttribute("successMessage", "Medication deleted successfully");
        return "redirect:/admin/medications";
    }
}
