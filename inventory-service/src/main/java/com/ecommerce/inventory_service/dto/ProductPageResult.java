package com.ecommerce.inventory_service.dto;


import com.ecommerce.inventory_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageResult {
    private List<Product> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
}
