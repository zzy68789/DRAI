package com.zzy.drai.domain;

import java.time.LocalDateTime;

public record AppUserRecord(
        long id,
        String username,
        String email,
        String passwordHash,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
