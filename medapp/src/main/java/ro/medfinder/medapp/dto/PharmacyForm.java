package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PharmacyForm {

    private Long id;

    @NotBlank(message = "Pharmacy name is required")
    private String name;

    private String cui;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private Boolean syncEnabled = false;
    private String syncEndpointUrl;

    /** Only used by SUPER_USER to assign an owner. */
    private Long ownerId;
}
