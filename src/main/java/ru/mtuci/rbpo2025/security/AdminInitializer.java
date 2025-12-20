package ru.mtuci.rbpo2025.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mtuci.rbpo2025.model.AppUser;
import ru.mtuci.rbpo2025.model.Role;
import ru.mtuci.rbpo2025.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Set;

@Component
@Profile("local")
public class AdminInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(ADMIN_USERNAME)) {
            return;
        }

        String rawPassword = generatePassword(16);

        AppUser admin = new AppUser();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRoles(Set.of(Role.ADMIN, Role.USER));

        userRepository.save(admin);

        System.out.println();
        System.out.println("==================================================");
        System.out.println("✅ Admin created");
        System.out.println("   username: " + ADMIN_USERNAME);
        System.out.println("   password: " + rawPassword);
        System.out.println("==================================================");
        System.out.println();
    }

    private String generatePassword(int length) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
