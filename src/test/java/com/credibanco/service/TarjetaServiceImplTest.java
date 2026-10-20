package com.credibanco.service;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.exception.BusinessException;
import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoTarjeta;
import com.credibanco.models.Transaccion;
import com.credibanco.models.Usuario;
import com.credibanco.repository.TarjetaRepository;
import com.credibanco.security.AccesoTarjeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarjetaServiceImplTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private TransaccionService transaccionService;

    @Mock
    private AccesoTarjeta accesoTarjeta;

    private TarjetaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TarjetaServiceImpl(tarjetaRepository, transaccionService, accesoTarjeta);
    }

    @Test
    void crearTarjeta_tipoNulo_rechaza() {
        TarjetaRequest request = requestValido();
        request.setTipoTarjeta(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("El tipo de tarjeta es obligatorio", ex.getMessage());
        verifyNoInteractions(tarjetaRepository);
    }

    @Test
    void crearTarjeta_titularNulo_rechaza() {
        TarjetaRequest request = requestValido();
        request.setNombreTitular(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("El nombre del titular debe incluir primer nombre y apellido", ex.getMessage());
        verifyNoInteractions(tarjetaRepository);
    }

    @Test
    void crearTarjeta_titularEnBlanco_rechaza() {
        TarjetaRequest request = requestValido();
        request.setNombreTitular("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("El nombre del titular debe incluir primer nombre y apellido", ex.getMessage());
    }

    @Test
    void crearTarjeta_titularSinApellido_rechaza() {
        TarjetaRequest request = requestValido();
        request.setNombreTitular("Ana");

        assertThrows(BusinessException.class, () -> service.crearTarjeta(request));
        verify(tarjetaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void crearTarjeta_sinFechaDeCreacion_rechaza() {
        numeroDisponible();
        TarjetaRequest request = requestValido();
        request.setFechaCreacion(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("La fecha de vencimiento debe ser al menos 3 años superior a la de creación", ex.getMessage());
        verify(tarjetaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void crearTarjeta_sinFechaDeVencimiento_rechaza() {
        numeroDisponible();
        TarjetaRequest request = requestValido();
        request.setFechaVencimiento(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("La fecha de vencimiento debe ser al menos 3 años superior a la de creación", ex.getMessage());
    }

    @Test
    void crearTarjeta_vencimientoMenorATresAnios_rechaza() {
        numeroDisponible();
        TarjetaRequest request = requestValido();
        request.setFechaVencimiento(LocalDate.of(2022, 12, 31));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(request));

        assertEquals("La fecha de vencimiento debe ser al menos 3 años superior a la de creación", ex.getMessage());
    }

    @Test
    void crearTarjeta_vigenciaExactaDeTresAnios_quedaActiva() {
        numeroDisponible();
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        when(accesoTarjeta.usuarioActual()).thenReturn(usuario);

        Tarjeta tarjeta = service.crearTarjeta(requestValido());

        assertEquals("Ana Lopez", tarjeta.getNombreTitular());
        assertTrue(tarjeta.getNumeroTarjeta().matches(TipoTarjeta.CREDITO.getIdProducto() + "\\d{10}"));
        assertEquals(LocalDate.of(2020, 1, 1), tarjeta.getFechaCreacion());
        assertEquals(LocalDate.of(2023, 1, 1), tarjeta.getFechaVencimiento());
        assertEquals(0, tarjeta.getSaldo().compareTo(BigDecimal.ZERO));
        assertEquals(Moneda.USD, tarjeta.getMoneda());
        assertSame(usuario, tarjeta.getUsuario());
        verify(tarjetaRepository).save(tarjeta);
    }

    @Test
    void crearTarjeta_normalizaEspaciosDelTitular() {
        numeroDisponible();
        TarjetaRequest request = requestValido();
        request.setNombreTitular("  Ana\tLopez  ");
        request.setTipoTarjeta(TipoTarjeta.DEBITO);

        Tarjeta tarjeta = service.crearTarjeta(request);

        assertEquals("Ana Lopez", tarjeta.getNombreTitular());
        assertTrue(tarjeta.getNumeroTarjeta().startsWith(TipoTarjeta.DEBITO.getIdProducto()));
        assertNull(tarjeta.getUsuario());
    }

    @Test
    void crearTarjeta_reintentaHastaEncontrarNumeroLibre() {
        Tarjeta existente = new Tarjeta();
        when(tarjetaRepository.findByNumeroTarjeta(anyString())).thenReturn(
                Optional.of(existente),
                Optional.of(existente),
                Optional.of(existente),
                Optional.of(existente),
                Optional.empty());

        Tarjeta tarjeta = service.crearTarjeta(requestValido());

        assertTrue(tarjeta.getNumeroTarjeta().matches("\\d{16}"));
        verify(tarjetaRepository).save(tarjeta);
    }

    @Test
    void crearTarjeta_cincoColisiones_rechaza() {
        Tarjeta existente = new Tarjeta();
        when(tarjetaRepository.findByNumeroTarjeta(anyString())).thenReturn(Optional.of(existente));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crearTarjeta(requestValido()));

        assertEquals("No fue posible generar un número de tarjeta único", ex.getMessage());
        verify(tarjetaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void recargarSaldo_delegaEnLaTransaccion() {
        Tarjeta tarjeta = new Tarjeta();
        Transaccion tx = new Transaccion();
        tx.setTarjeta(tarjeta);
        BigDecimal monto = new BigDecimal("150");
        when(transaccionService.recargar(8L, monto)).thenReturn(tx);

        assertSame(tarjeta, service.recargarSaldo(8L, monto));
    }

    @Test
    void listarTarjetas_sinSesion_devuelveTodas() {
        when(accesoTarjeta.usuarioActual()).thenReturn(null);
        when(tarjetaRepository.findAll()).thenReturn(List.of(new Tarjeta()));

        assertEquals(1, service.listarTarjetas().size());
    }

    @Test
    void listarTarjetas_conSesion_filtraPorUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        when(accesoTarjeta.usuarioActual()).thenReturn(usuario);
        when(tarjetaRepository.findByUsuarioId(9L)).thenReturn(List.of(new Tarjeta(), new Tarjeta()));

        assertEquals(2, service.listarTarjetas().size());
        verify(tarjetaRepository, never()).findAll();
    }

    private void numeroDisponible() {
        when(tarjetaRepository.findByNumeroTarjeta(anyString())).thenReturn(Optional.empty());
    }

    private TarjetaRequest requestValido() {
        TarjetaRequest request = new TarjetaRequest();
        request.setNombreTitular("Ana Lopez");
        request.setFechaCreacion(LocalDate.of(2020, 1, 1));
        request.setFechaVencimiento(LocalDate.of(2023, 1, 1));
        request.setTipoTarjeta(TipoTarjeta.CREDITO);
        return request;
    }
}
