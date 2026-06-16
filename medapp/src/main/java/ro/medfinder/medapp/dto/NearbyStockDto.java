package ro.medfinder.medapp.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class NearbyStockDto {
    private BigDecimal price;
    private Integer quantity;
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
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
    }
}
