package com.ecommerce.user_service.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "users")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String password;

        @Column(nullable = false)
        private String fullName;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Role role = Role.CUSTOMER;

        @Column(updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        public enum Role {
            CUSTOMER, ADMIN
        }
    }
