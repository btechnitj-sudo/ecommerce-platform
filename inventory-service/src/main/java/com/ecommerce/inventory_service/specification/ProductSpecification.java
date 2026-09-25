package com.ecommerce.inventory_service.specification;


import com.ecommerce.inventory_service.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> withFilters(String search, String category) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (search != null && !search.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }

            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }

            return predicate;
        };
    }
}
