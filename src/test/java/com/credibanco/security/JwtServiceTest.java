package com.credibanco.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "credibanco-test-jwt-secret-key!!!";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000);
    }

    @Test
    void generaYRecuperaElUsuario() {
        String token = jwtService.generate("ana");

        assertEquals("ana", jwtService.extractUsername(token));
    }

    @Test
    void tokenVigenteDelMismoUsuarioEsValido() {
        String token = jwtService.generate("ana");

        assertTrue(jwtService.isValid(token, usuario("ana")));
    }

    @Test
    void tokenDeOtroUsuarioNoEsValido() {
        String token = jwtService.generate("ana");

        assertFalse(jwtService.isValid(token, usuario("luis")));
    }

    @Test
    void tokenVencido_elParserLoRechaza() {
        JwtService vencido = new JwtService(SECRET, -60_000);
        String token = vencido.generate("ana");

        assertThrows(JwtException.class, () -> vencido.isValid(token, usuario("ana")));
    }

    @Test
    void tokenMalformadoLanzaJwtException() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("no-es-un-jwt"));
    }

    private UserDetails usuario(String username) {
        return User.withUsername(username).password("x").authorities("USER").build();
    }
}
