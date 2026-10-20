package com.credibanco.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig(mock(JwtAuthFilter.class));
    }

    @Test
    void elFiltroJwtNoSeRegistraDosVeces() {
        FilterRegistrationBean<JwtAuthFilter> registration = config.jwtFilterRegistration(mock(JwtAuthFilter.class));

        assertFalse(registration.isEnabled());
    }

    @Test
    void passwordEncoderCifraConBcrypt() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertTrue(encoder.matches("secreto", encoder.encode("secreto")));
    }

    @Test
    void authenticationManagerSeTomaDeLaConfiguracion() throws Exception {
        AuthenticationConfiguration configuration = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(configuration.getAuthenticationManager()).thenReturn(manager);

        assertSame(manager, config.authenticationManager(configuration));
    }

    @Test
    void peticionSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/tarjetas"))
                .andExpect(status().isUnauthorized());
    }
}
