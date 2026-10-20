package com.credibanco.security;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.Usuario;
import com.credibanco.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccesoTarjetaTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private AccesoTarjeta accesoTarjeta;

    @BeforeEach
    void setUp() {
        accesoTarjeta = new AccesoTarjeta(usuarioRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinAutenticacion_retornaNull() {
        assertNull(accesoTarjeta.usuarioActual());
    }

    @Test
    void autenticacionNoConfirmada_retornaNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertNull(accesoTarjeta.usuarioActual());
    }

    @Test
    void nombreNulo_retornaNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertNull(accesoTarjeta.usuarioActual());
    }

    @Test
    void usuarioAnonimo_retornaNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertNull(accesoTarjeta.usuarioActual());
    }

    @Test
    void usuarioAutenticadoQueNoExiste_retornaNull() {
        autenticar("ana");
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.empty());

        assertNull(accesoTarjeta.usuarioActual());
    }

    @Test
    void usuarioAutenticado_retornaLaCuenta() {
        autenticar("ana");
        Usuario usuario = new Usuario();
        usuario.setId(4L);
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));

        assertSame(usuario, accesoTarjeta.usuarioActual());
    }

    @Test
    void validar_sinUsuarioEnContexto_noRechaza() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setUsuario(dueno(1L));

        assertDoesNotThrow(() -> accesoTarjeta.validar(tarjeta));
    }

    @Test
    void validar_tarjetaSinDueno_noRechaza() {
        autenticarDueno(1L);
        Tarjeta tarjeta = new Tarjeta();

        assertDoesNotThrow(() -> accesoTarjeta.validar(tarjeta));
    }

    @Test
    void validar_mismoDueno_noRechaza() {
        autenticarDueno(1L);
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setUsuario(dueno(1L));

        assertDoesNotThrow(() -> accesoTarjeta.validar(tarjeta));
    }

    @Test
    void validar_otroDueno_rechaza() {
        autenticarDueno(1L);
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setUsuario(dueno(2L));

        BusinessException ex = assertThrows(BusinessException.class, () -> accesoTarjeta.validar(tarjeta));

        assertEquals("Tarjeta no encontrada", ex.getMessage());
    }

    private void autenticar(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(username);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void autenticarDueno(Long id) {
        autenticar("ana");
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
    }

    private Usuario dueno(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }
}
