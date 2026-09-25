package com.ecommerce.order_service.client;

import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.dto.StockUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    private final RestClient restClient;

    @Autowired
    public InventoryClient(@Value("${services.inventory.url}") String inventoryUrl){
        this.restClient=RestClient.builder()
                .baseUrl(inventoryUrl)
                .build();
    }

    public ProductResponse getProduct(Long productId){
        return restClient.get()
                .uri("/api/products/{id}",productId)
                .retrieve()
                .body(ProductResponse.class);
    }

    public void decreaseStock(Long productId,Integer quantity){
        restClient.patch()
                .uri("/api/products/{id}/decrease-stock", productId)
                .body(new StockUpdateRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    public void increaseStock(Long productId, Integer quantity){
        restClient.patch()
                .uri("/api/products/{id}/increase-stock", productId)
                .body(new StockUpdateRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
}
