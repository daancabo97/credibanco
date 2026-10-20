package com.credibanco.security;

import com.credibanco.models.Usuario;
import com.credibanco.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void cargaUsuarioExistenteConRolUser() {
        Usuario usuario = new Usuario();
        usuario.setUsername("ana");
        usuario.setPassword("hash");
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));

        UserDetails details = usuarioDetailsService.loadUserByUsername("ana");

        assertEquals("ana", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> "USER".equals(a.getAuthority())));
    }

    @Test
    void usuarioInexistente_lanzaUsernameNotFound() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("nadie"));

        assertEquals("nadie", ex.getMessage());
    }
}
