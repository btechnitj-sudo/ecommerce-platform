package com.ecommerce.notification_service.kafka;



import com.ecommerce.notification_service.entity.Notification;
import com.ecommerce.notification_service.event.OrderPlacedEvent;
import com.ecommerce.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "order-placed", groupId = "notification-service-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order-placed event for orderId={}", event.getOrderId());

        String message = String.format(
                "Your order #%d for ₹%s has been confirmed!",
                event.getOrderId(),
                event.getTotalAmount()
        );

        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setOrderId(event.getOrderId());
        notification.setMessage(message);

        notificationRepository.save(notification);
        log.info("Saved notification for userId={}", event.getUserId());
    }
}