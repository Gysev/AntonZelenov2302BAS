package ru.mtuci.rbpo2025.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.mtuci.rbpo2025.dto.auth.RegisterRequest;
import ru.mtuci.rbpo2025.model.AppUser;
import ru.mtuci.rbpo2025.model.Role;
import ru.mtuci.rbpo2025.repository.UserRepository;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
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

    private boolean isStrongPassword(String p) {
        if (p == null || p.length() < 8) return false;
        boolean hasUpper = p.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = p.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = p.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = p.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{};':\",.<>/?\\|`~".indexOf(ch) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
