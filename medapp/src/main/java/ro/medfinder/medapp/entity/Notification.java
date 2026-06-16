package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.medfinder.medapp.entity.enums.NotificationStatus;
import ro.medfinder.medapp.entity.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Notificare trimisă unui utilizator (EMAIL, SMS sau IN_APP).
 * Poate fi legată opțional de o comandă.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"recipient", "relatedOrder"})
@EqualsAndHashCode(callSuper = true, exclude = {"recipient", "relatedOrder"})
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 15)
    private NotificationType type;

    @Column(name = "subject", length = 300)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private NotificationStatus status;

    @Column(name = "target_address")
    private String targetAddress;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ── Relații ──────────────────────────────────────────────────

    /** Destinatarul notificării. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    /** Comanda asociată (opțional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_order_id")
    private Order relatedOrder;
}
