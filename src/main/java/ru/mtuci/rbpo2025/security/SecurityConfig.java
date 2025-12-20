package ru.mtuci.rbpo2025.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;

    public SecurityConfig(AppUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain publicEndpoints(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/csrf", "/error")
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName("XSRF-TOKEN");
        csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        csrfTokenRepository.setCookiePath("/");
        
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/auth/register")
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/info").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/senders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/senders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/senders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/senders/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/recipients/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/recipients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/recipients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/recipients/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/couriers/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/couriers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/couriers/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/deliveries/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/deliveries/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/deliveries/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/deliveries/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/business/orders").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/complete").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/couriers/*/deliveries").hasAnyRole("USER", "ADMIN")
                        
                        .requestMatchers(HttpMethod.POST, "/api/business/couriers/unavailable/redistribute").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/business/deliveries/smart-assign").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/business/deliveries/schedule").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/business/orders/partial-cancel").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/business/deliveries/check-sla").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
