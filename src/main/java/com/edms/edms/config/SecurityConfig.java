package com.edms.edms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers(
                                "/auth/**",
                                "/auth.html",
                                "/css/**",
                                "/js/**",
                                "/register",
                                "/favicon.ico"
                        ).permitAll()

                        // ADMIN ONLY
                        .requestMatchers("/admin/**", "/admin*", "/admin-ui", "/admin.html")
                        .hasRole("ADMIN")

                        // USER + ADMIN
                        .requestMatchers(
                                "/documents/**",
                                "/folders/**",
                                "/genai/**",
                                "/documents-ui"
                        ).hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
