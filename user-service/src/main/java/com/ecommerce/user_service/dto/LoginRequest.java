package com.ecommerce.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Eail is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
