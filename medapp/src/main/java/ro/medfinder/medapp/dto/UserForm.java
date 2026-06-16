package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private Boolean enabled = true;

    /** Only for PHARM_OWNER. */
    private String companyName;

    /** Only for PHARMACIST — assign to location. */
    private Long locationId;

    /** Read-only — used to show role in the form. */
    private String role;
}
