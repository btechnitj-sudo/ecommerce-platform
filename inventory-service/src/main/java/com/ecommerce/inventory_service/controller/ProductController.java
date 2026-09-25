package com.ecommerce.inventory_service.controller;

import com.ecommerce.inventory_service.dto.*;
import com.ecommerce.inventory_service.entity.Product;
import com.ecommerce.inventory_service.entity.Review;
import com.ecommerce.inventory_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<ProductPageResult> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ){
        return ResponseEntity.ok(productService.getAllProducts(page, size, search, category));
    }
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(){
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/{id}")
    public  ResponseEntity<Product> getProductById(@PathVariable Long id){

        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request){
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/decrease-stock")
    public  ResponseEntity<Product> decreaseStock(@PathVariable long id, @Valid @RequestBody StockUpdateRequest request){
        return ResponseEntity.ok(productService.decreaseStock(id,request.getQuantity()));
    }

    @PatchMapping("/{id}/increase-stock")
    public ResponseEntity<Product> increaseStock(@PathVariable long id, @Valid @RequestBody StockUpdateRequest request){
        return  ResponseEntity.ok(productService.increaseProduct(id, request.getQuantity()));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Review> addReview(@PathVariable Long id, @Valid @RequestBody CreateReviewRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addReview(id, request));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long id){
        return ResponseEntity.ok(productService.getReviews(id));
    }

}