package ro.medfinder.medapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SyncRequestForm {
    private Long pharmacyId;
    private String syncType; // "CSV" or "MANUAL"
    private MultipartFile file;
}
