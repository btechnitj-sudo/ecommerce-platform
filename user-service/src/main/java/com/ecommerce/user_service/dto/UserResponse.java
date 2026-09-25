package com.ecommerce.user_service.dto;

import com.ecommerce.user_service.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {
    private long id;
    private String email;
    private String fullName;
    private User.Role role;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getCreatedAt()
        );
    }
    }

