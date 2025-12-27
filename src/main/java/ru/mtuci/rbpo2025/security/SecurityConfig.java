package ru.mtuci.rbpo2025.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AppUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/auth/**", "/api/senders", "/api/senders/**", "/api/recipients/**", 
                        "/api/parcels/**", "/api/couriers/**", "/api/deliveries/**", "/api/business/**")
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll();
                    
                    auth.requestMatchers(HttpMethod.GET, "/api/senders").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/senders/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/senders/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/senders/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/senders/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/recipients/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/recipients/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/recipients/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/recipients/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/parcels/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/couriers/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/couriers/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/couriers/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/couriers/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/deliveries/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/deliveries/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/deliveries/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/deliveries/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/api/business/orders").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/deliveries/*/complete").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/couriers/*/deliveries").hasAnyRole("USER", "ADMIN");
                    
                    auth.requestMatchers(HttpMethod.POST, "/api/business/couriers/unavailable/redistribute").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/smart-assign").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/schedule").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/orders/partial-cancel").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/check-sla").hasRole("ADMIN");

                    auth.anyRequest().authenticated();
                });
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain basicAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/info", "/error").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
                    
                    auth.requestMatchers(HttpMethod.GET, "/api/senders").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/senders/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/senders/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/senders/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/senders/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/recipients/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/recipients/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/recipients/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/recipients/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/parcels/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/couriers/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/couriers/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/couriers/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/couriers/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/deliveries/**").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/deliveries/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/deliveries/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/deliveries/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/api/business/orders").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/deliveries/*/complete").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/couriers/*/deliveries").hasAnyRole("USER", "ADMIN");
                    
                    auth.requestMatchers(HttpMethod.POST, "/api/business/couriers/unavailable/redistribute").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/smart-assign").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/schedule").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/orders/partial-cancel").hasAnyRole("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/business/deliveries/check-sla").hasRole("ADMIN");

                    auth.anyRequest().authenticated();
                });
        return http.build();
    }
}
