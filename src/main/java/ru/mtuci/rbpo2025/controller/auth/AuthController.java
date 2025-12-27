package ru.mtuci.rbpo2025.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.mtuci.rbpo2025.dto.auth.LoginRequest;
import ru.mtuci.rbpo2025.dto.auth.RefreshRequest;
import ru.mtuci.rbpo2025.dto.auth.RegisterRequest;
import ru.mtuci.rbpo2025.model.AppUser;
import ru.mtuci.rbpo2025.model.Role;
import ru.mtuci.rbpo2025.repository.UserRepository;
import ru.mtuci.rbpo2025.service.TokenService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    public AuthController(UserRepository users, PasswordEncoder encoder, TokenService tokenService) {
        this.users = users;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();

        if (username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username exists");
        }
        if (!isStrongPassword(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weak password");
        }
        if (username.equalsIgnoreCase("admin")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot register as admin");
        }

        AppUser u = new AppUser()
                .setUsername(username)
                .setPassword(encoder.encode(password))
                .setRoles(Set.of(Role.USER));

        users.save(u);

        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "roles", u.getRoles().stream().map(Enum::name).toList()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();

        if (username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }

        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Map<String, String> tokens = tokenService.generateTokenPair(user);

        return Map.of(
                "accessToken", tokens.get("accessToken"),
                "refreshToken", tokens.get("refreshToken")
        );
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@Valid @RequestBody RefreshRequest req) {
        String refreshToken = req.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken required");
        }

        try {
            Map<String, String> tokens = tokenService.refreshTokenPair(refreshToken);
            return Map.of(
                    "accessToken", tokens.get("accessToken"),
                    "refreshToken", tokens.get("refreshToken")
            );
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private boolean isStrongPassword(String p) {
        if (p == null || p.length() < 8) return false;
        boolean hasUpper = p.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = p.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = p.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = p.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{};':\",.<>/?\\|`~".indexOf(ch) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
