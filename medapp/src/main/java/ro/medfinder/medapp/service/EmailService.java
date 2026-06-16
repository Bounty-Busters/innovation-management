package ro.medfinder.medapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ro.medfinder.medapp.entity.Notification;
import ro.medfinder.medapp.entity.Order;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOrderStatusEmail(Notification notification, Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getTargetAddress());
            helper.setSubject(notification.getSubject());
            helper.setFrom("no-reply@medfinder.ro");

            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("notificationMessage", notification.getMessage());

            String htmlContent = templateEngine.process("mail/order-status", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to {}", notification.getTargetAddress());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", notification.getTargetAddress(), e);
            throw new RuntimeException("Mail sending failed", e);
        }
    }
}
