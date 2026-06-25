package com.zzy.drai.auth;

public record TokenClaims(long userId, String username, String role, long expiresAtEpochSecond) {
}
