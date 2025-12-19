package ru.mtuci.rbpo2025.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Привет, Антон! 🚀 Spring Boot работает!";
    }

    @GetMapping("/info")
    public String info() {
        return "Это твой тестовый контроллер для RBPO_2025.";
    }

    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
