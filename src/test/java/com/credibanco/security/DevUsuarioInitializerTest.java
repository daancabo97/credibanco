package com.credibanco.security;

import com.credibanco.models.Usuario;
import com.credibanco.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevUsuarioInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void usuarioNulo_noCreaNada() {
        DevUsuarioInitializer initializer = initializer(null, "admin123");

        initializer.run(null);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void usuarioEnBlanco_noCreaNada() {
        DevUsuarioInitializer initializer = initializer("   ", "admin123");

        initializer.run(null);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void passwordNula_noCreaNada() {
        DevUsuarioInitializer initializer = initializer("admin", null);

        initializer.run(null);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void passwordEnBlanco_noCreaNada() {
        DevUsuarioInitializer initializer = initializer("admin", "  ");

        initializer.run(null);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void usuarioYaExiste_noLoReescribe() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(new Usuario()));
        DevUsuarioInitializer initializer = initializer("admin", "admin123");

        initializer.run(null);

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void usuarioNuevo_guardaLaClaveCifrada() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("$2a$hash");
        DevUsuarioInitializer initializer = initializer("admin", "admin123");

        initializer.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("$2a$hash", captor.getValue().getPassword());
    }

    private DevUsuarioInitializer initializer(String username, String password) {
        return new DevUsuarioInitializer(usuarioRepository, passwordEncoder, username, password);
    }
}
