package ro.medfinder.medapp.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.medfinder.medapp.entity.Notification;
import ro.medfinder.medapp.entity.Order;
import ro.medfinder.medapp.entity.enums.NotificationStatus;
import ro.medfinder.medapp.entity.enums.NotificationType;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.NotificationRepository;
import ro.medfinder.medapp.service.EmailService;
import ro.medfinder.medapp.dto.NotificationDto;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        OrderStatus status = order.getStatus();

        if (status == OrderStatus.PENDING || status == OrderStatus.PICKED_UP) {
            return; 
        }

        String subject = "Order " + order.getOrderNumber() + " update!";
        String message = buildMessageForStatus(status, order);

        Notification notification = Notification.builder()
                .type(NotificationType.IN_APP) 
                .subject(subject)
                .message(message)
                .status(NotificationStatus.SENT)
                .targetAddress(order.getClient().getEmail())
                .recipient(order.getClient())
                .relatedOrder(order)
                .sentAt(LocalDateTime.now())
                .build();

        try {
            // Re-assign the reference to get the generated ID reliably
            notification = notificationRepository.saveAndFlush(notification);
            
            // Send Email
            emailService.sendOrderStatusEmail(notification, order);

            // Send WebSocket message to the client using DTO
            NotificationDto dto = NotificationDto.builder()
                    .id(notification.getId())
                    .subject(notification.getSubject())
                    .message(notification.getMessage())
                    .sentAt(notification.getSentAt())
                    .orderNumber(order.getOrderNumber())
                    .build();
                    
            messagingTemplate.convertAndSend("/topic/client/" + order.getClient().getId(), dto);
            
        } catch (Exception e) {
            log.error("Failed to process notification for order {}", order.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    private String buildMessageForStatus(OrderStatus status, Order order) {
        switch (status) {
            case ACCEPTED:
                return "Your order has been accepted by the pharmacy! You can pick it up until " + order.getExpiresAt();
            case READY_FOR_PICKUP:
                return "Your order is ready for pickup at " + order.getPickupLocation().getName();
            case REJECTED:
                return "Your order was rejected. Reason: " + (order.getRejectionReason() != null ? order.getRejectionReason() : "N/A");
            case CANCELLED:
                return "Your order was cancelled by the pharmacist.";
            case EXPIRED:
                return "Your order has expired because it was not picked up in time.";
            default:
                return "Your order status changed to " + status;
        }
    }
}
