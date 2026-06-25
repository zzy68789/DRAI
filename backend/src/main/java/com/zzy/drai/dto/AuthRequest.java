package com.zzy.drai.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String username,
        String email,
        @NotBlank String password
) {
}
