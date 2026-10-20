package com.credibanco.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioDetailsService usuarioDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter filter;

    private final UserDetails usuario = User.withUsername("ana").password("x").authorities("USER").build();

    @BeforeEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void soltarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinAuthorization_dejaPasarLaPeticion() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void encabezadoQueNoEsBearer_dejaPasarLaPeticion() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void tokenValido_autenticaAlUsuario() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-ok");
        when(jwtService.extractUsername("token-ok")).thenReturn("ana");
        when(usuarioDetailsService.loadUserByUsername("ana")).thenReturn(usuario);
        when(jwtService.isValid("token-ok", usuario)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals("ana", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void usuarioNulo_noCargaDetalles() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-ok");
        when(jwtService.extractUsername("token-ok")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(usuarioDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void sesionYaAutenticada_noVuelveAValidar() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        when(request.getHeader("Authorization")).thenReturn("Bearer token-ok");
        when(jwtService.extractUsername("token-ok")).thenReturn("ana");

        filter.doFilter(request, response, filterChain);

        verify(usuarioDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.any());
        verify(jwtService, never()).isValid(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tokenNoValido_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-ok");
        when(jwtService.extractUsername("token-ok")).thenReturn("ana");
        when(usuarioDetailsService.loadUserByUsername("ana")).thenReturn(usuario);
        when(jwtService.isValid("token-ok", usuario)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtInvalido_limpiaElContexto() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previo", null));
        when(request.getHeader("Authorization")).thenReturn("Bearer malo");
        when(jwtService.extractUsername("malo")).thenThrow(new JwtException("firma"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void argumentoIlegal_limpiaElContexto() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previo", null));
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtService.extractUsername("")).thenThrow(new IllegalArgumentException("vacio"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
