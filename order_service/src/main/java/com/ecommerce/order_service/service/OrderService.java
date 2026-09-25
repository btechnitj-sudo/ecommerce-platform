package com.ecommerce.order_service.service;

import com.ecommerce.order_service.client.InventoryClient;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.exception.ConflictException;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.kafka.OrderEventProducer;
import com.ecommerce.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final OrderEventProducer orderEventProducer;

    private  final PaymentService paymentService;

    @Transactional
    public Order placeOrder(CreateOrderRequest request){
        Order order =new Order();
        order.setUserId(request.getUserId());
        BigDecimal total = BigDecimal.ZERO;

        //validate stock and build order items before touching inventory

        for(OrderItemRequest itemRequest: request.getItems()){
            ProductResponse product=inventoryClient.getProduct(itemRequest.getProductId());

            if(product.getStockQuantity()< itemRequest.getQuantity()){
                throw new ConflictException("Insufficient stock for product:" + product.getName());
            }
            OrderItem item= new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());

            order.addItem(item);

            total=total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(total);

        boolean paymentSuccess = paymentService.processPayment(total);

        if (!paymentSuccess) {
            order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
            return orderRepository.save(order);
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);

        //decrease after confirmed everything is valid
        for(OrderItem item:order.getItems()){
            inventoryClient.decreaseStock(item.getProductId(), item.getQuantity());
        }
        Order savedOrder=orderRepository.save(order);

        //publish the evetn after order is safely saved
        publishOrderEvent(savedOrder);
        return savedOrder;

    }
    private void publishOrderEvent(Order order){
        List<OrderPlacedEvent.OrderItemEvent> itemEvents=new ArrayList<>();

        for(OrderItem item: order.getItems()){
            itemEvents.add(new OrderPlacedEvent.OrderItemEvent(item.getProductId(),item.getQuantity()));}
        OrderPlacedEvent event=new OrderPlacedEvent(
                order.getId(),
                order.getUserId(),
                itemEvents,
                order.getTotalAmount(),
                order.getCreatedAt()
        );
        orderEventProducer.publishOrderPlaced(event);

    }

    public List<Order> getOrderByUser(Long userId){
        return orderRepository.findByUserId(userId);
    }

    public Order getOderById(Long id){
        return orderRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Order not found with id " + id));
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId){
        Order order = getOderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new ConflictException("Order cannot be cancelled — current status is " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            inventoryClient.increaseStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
