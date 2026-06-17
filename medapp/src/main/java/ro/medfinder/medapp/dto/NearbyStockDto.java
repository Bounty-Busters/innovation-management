package ro.medfinder.medapp.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class NearbyStockDto {
    private Long id;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime updatedAt;
    private MedicationDto medication;
    private LocationDto location;

    @Data
    @Builder
    public static class MedicationDto {
        private String ean;
        private String name;
        private String activeSubstance;
        private String dosage;
        private Boolean prescriptionRequired;
        private String form;
    }

    @Data
    @Builder
    public static class LocationDto {
        private Long id;
        private String name;
        private String pharmacyName;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
    }
}
