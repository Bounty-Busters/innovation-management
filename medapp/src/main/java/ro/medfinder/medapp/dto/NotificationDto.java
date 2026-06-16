package ro.medfinder.medapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private String subject;
    private String message;
    private LocalDateTime sentAt;
    private String orderNumber;
}
