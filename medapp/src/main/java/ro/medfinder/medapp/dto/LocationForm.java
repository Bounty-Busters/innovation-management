package ro.medfinder.medapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LocationForm {

    private Long id;

    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "County is required")
    private String county;

    private String postalCode;
    private Double latitude;
    private Double longitude;
    private String phone;
    private Boolean active = true;

    @NotNull(message = "Pharmacy is required")
    private Long pharmacyId;

    /** Working hours for each day of the week (0=MONDAY .. 6=SUNDAY). */
    private List<WorkingHourEntry> workingHours = new ArrayList<>();

    @Getter
    @Setter
    public static class WorkingHourEntry {
        private String dayOfWeek;
        private String openTime;
        private String closeTime;
        private Boolean closed = false;
    }
}
