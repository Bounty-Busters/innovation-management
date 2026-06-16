package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ro.medfinder.medapp.entity.enums.MedForm;

@Getter
@Setter
public class MedicationForm {

    private Long id;

    @NotBlank(message = "EAN is required")
    private String ean;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Active substance is required")
    private String activeSubstance;

    @NotNull(message = "Form is required")
    private MedForm form;

    private String dosage;

    @NotBlank(message = "Category is required")
    private String category;

    private Boolean prescriptionRequired = false;

    private String description;
}
