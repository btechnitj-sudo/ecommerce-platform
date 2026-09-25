package com.ecommerce.order_service.kafka;

import com.ecommerce.order_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {
    private static final String TOPIC="order-placed";

    private final KafkaTemplate<String , OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event){
        log.info("publishing order-placed event for order Id={}",event.getOrderId());
        kafkaTemplate.send(TOPIC,event.getOrderId().toString(),event);
    }
}
