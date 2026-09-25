package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.CreateProductRequest;
import com.ecommerce.inventory_service.dto.ProductPageResult;
import com.ecommerce.inventory_service.dto.UpdateProductRequest;
import com.ecommerce.inventory_service.entity.Product;
import com.ecommerce.inventory_service.repository.ProductRepository;
import com.ecommerce.inventory_service.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.ecommerce.inventory_service.specification.ProductSpecification;
import com.ecommerce.inventory_service.entity.Review;
import com.ecommerce.inventory_service.dto.CreateReviewRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ReviewRepository reviewRepository;

    @Cacheable(value = "products", key = "#page + '-' + #size + '-' + #search + '-' + #category")
    public ProductPageResult getAllProducts(int page, int size, String search, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = ProductSpecification.withFilters(search, category);
        Page<Product> result = productRepository.findAll(spec, pageable);
        return new ProductPageResult(result.getContent(), result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Cacheable("categories")
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    public Product createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());

        return productRepository.save(product);
    }

    public List<Product> getAllProductsRaw() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not find with id " + id));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product decreaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product increaseProduct(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Review addReview(Long productId, CreateReviewRequest request){
        Product product = getProductById(productId);

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(request.getUserId());
        review.setReviewerName(request.getReviewerName());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        List<Review> allReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        double average = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(Math.round(average * 10.0) / 10.0);
        product.setReviewCount(allReviews.size());
        productRepository.save(product);

        return review;
    }

    public List<Review> getReviews(Long productId){
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
}