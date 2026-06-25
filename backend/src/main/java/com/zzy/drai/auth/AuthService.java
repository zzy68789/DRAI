package com.zzy.drai.auth;

import com.zzy.drai.domain.AppUserRecord;
import com.zzy.drai.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthService(AppUserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public AuthenticatedUser register(String username, String email, String password) {
        validate(username, password);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        AppUserRecord user = userRepository.create(username, email, passwordHasher.hash(password), "USER");
        return toAuthenticatedUser(user);
    }

    public AuthenticatedUser login(String username, String password) {
        AppUserRecord user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        return toAuthenticatedUser(user);
    }

    public AuthenticatedUser resolveToken(String token) {
        TokenClaims claims = tokenService.verify(token);
        return new AuthenticatedUser(claims.userId(), claims.username(), "", claims.role(), token);
    }

    private AuthenticatedUser toAuthenticatedUser(AppUserRecord user) {
        String token = tokenService.issue(user.id(), user.username(), user.role());
        return new AuthenticatedUser(user.id(), user.username(), user.email(), user.role(), token);
    }

    private void validate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空，密码至少 6 位");
        }
    }
}
