package ro.medfinder.medapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.medfinder.medapp.entity.Client;
import ro.medfinder.medapp.entity.Notification;
import ro.medfinder.medapp.entity.enums.NotificationStatus;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.repository.NotificationRepository;
import ro.medfinder.medapp.dto.NotificationDto;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;

    @GetMapping("/me")
    public ResponseEntity<Long> getMyClientId(Principal principal) {
        if (principal == null) return ResponseEntity.ok(-1L);
        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(principal.getName()))
                .findFirst()
                .orElse(null);
        return ResponseEntity.ok(client != null ? client.getId() : -1L);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(principal.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Logged in client not found in DB"));

        List<Notification> unread = notificationRepository
                .findByRecipientIdAndStatusOrderByCreatedAtDesc(client.getId(), NotificationStatus.SENT);

        List<NotificationDto> dtos = unread.stream().map(n -> NotificationDto.builder()
                .id(n.getId())
                .subject(n.getSubject())
                .message(n.getMessage())
                .sentAt(n.getSentAt())
                .orderNumber(n.getRelatedOrder() != null ? n.getRelatedOrder().getOrderNumber() : null)
                .build()).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(principal.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Logged in client not found in DB"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getRecipient().getId().equals(client.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }
}
