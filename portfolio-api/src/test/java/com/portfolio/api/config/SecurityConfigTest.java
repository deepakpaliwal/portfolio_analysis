package com.portfolio.api.config;

import com.portfolio.api.security.CustomUserDetailsService;
import com.portfolio.api.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        // Mock the dependencies required by SecurityConfig constructor
        JwtAuthenticationFilter jwtAuthenticationFilter = Mockito.mock(JwtAuthenticationFilter.class);
        CustomUserDetailsService customUserDetailsService = Mockito.mock(CustomUserDetailsService.class);
        
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, customUserDetailsService);
        passwordEncoder = securityConfig.passwordEncoder();
    }

    @Test
    public void testPasswordEncoding() {
        String rawPassword = "Password123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertTrue(securityConfig.verifyPassword(rawPassword, encodedPassword));
    }

    @Test
    public void testDefaultUserPasswordMatchesDbHash() {
        // Hash from 003-seed-sample-users.sql
        String dbHash = "$2a$12$l9RmyQ1CjFNmQlL/UtmPrO4jb9FDRmD4OhXrc86LB8Par2CG4W3ty";
        String rawPassword = "Password123!";

        assertTrue(securityConfig.verifyPassword(rawPassword, dbHash), 
            "The default password 'Password123!' does not match the hash in the database seed script.");
    }

    @Test
    public void testWrongPasswordDoesNotMatch() {
        String dbHash = "$2a$12$l9RmyQ1CjFNmQlL/UtmPrO4jb9FDRmD4OhXrc86LB8Par2CG4W3ty";
        String wrongPassword = "WrongPassword";

        assertFalse(securityConfig.verifyPassword(wrongPassword, dbHash));
    }
}
