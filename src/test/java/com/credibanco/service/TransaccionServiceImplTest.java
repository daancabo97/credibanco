package com.credibanco.service;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoMovimiento;
import com.credibanco.models.TipoTarjeta;
import com.credibanco.models.Transaccion;
import com.credibanco.repository.TarjetaRepository;
import com.credibanco.repository.TransaccionRepository;
import com.credibanco.security.AccesoTarjeta;
import com.credibanco.service.strategy.AccountBalanceValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private AccesoTarjeta accesoTarjeta;

    private TransaccionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransaccionServiceImpl(
                transaccionRepository, tarjetaRepository, accesoTarjeta, new AccountBalanceValidationStrategy());
    }

    @Test
    void compra_montoNulo_rechaza() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarCompra(1L, null));

        assertEquals("El monto debe ser mayor que cero", ex.getMessage());
        verify(tarjetaRepository, never()).findById(any());
    }

    @Test
    void compra_montoCero_rechaza() {
        assertThrows(BusinessException.class, () -> service.registrarCompra(1L, BigDecimal.ZERO));
    }

    @Test
    void compra_montoNegativo_rechaza() {
        assertThrows(BusinessException.class, () -> service.registrarCompra(1L, new BigDecimal("-1")));
    }

    @Test
    void recarga_montoNegativo_rechaza() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.recargar(1L, new BigDecimal("-5")));

        assertEquals("El monto debe ser mayor que cero", ex.getMessage());
    }

    @Test
    void compra_tarjetaInexistente_rechaza() {
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarCompra(1L, new BigDecimal("10")));

        assertEquals("Tarjeta no encontrada", ex.getMessage());
        verify(accesoTarjeta, never()).validar(any());
    }

    @Test
    void compra_tarjetaVencida_rechazaSinDescontar() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "100", LocalDate.now().minusDays(1));
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarCompra(1L, new BigDecimal("10")));

        assertEquals("Tarjeta vencida", ex.getMessage());
        assertEquals(EstadoTransaccion.RECHAZADA, ex.getEstado());
        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("100")));
        Transaccion guardada = transaccionGuardada();
        assertEquals(EstadoTransaccion.RECHAZADA, guardada.getEstado());
        assertEquals(TipoMovimiento.COMPRA, guardada.getTipoMovimiento());
        assertEquals(0, guardada.getMonto().compareTo(new BigDecimal("10")));
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void compra_sinFechaDeVencimiento_seRegistra() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "100", null);
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        Transaccion tx = service.registrarCompra(1L, new BigDecimal("25"));

        assertEquals(EstadoTransaccion.EXITOSA, tx.getEstado());
        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("75")));
    }

    @Test
    void compra_debitoConSaldo_descuentaYQuedaExitosa() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "100", LocalDate.now().plusYears(1));
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        Transaccion tx = service.registrarCompra(1L, new BigDecimal("30"));

        assertSame(tarjeta, tx.getTarjeta());
        assertEquals(0, tx.getMonto().compareTo(new BigDecimal("30")));
        assertEquals(Moneda.USD, tx.getMoneda());
        assertEquals(TipoMovimiento.COMPRA, tx.getTipoMovimiento());
        assertEquals(EstadoTransaccion.EXITOSA, tx.getEstado());
        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("70")));
        verify(accesoTarjeta).validar(tarjeta);
        verify(transaccionRepository).save(tx);
        verify(tarjetaRepository).save(tarjeta);
    }

    @Test
    void compra_creditoSinSaldo_relanzaElRechazo() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.CREDITO, "0", LocalDate.now().plusDays(10));
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarCompra(1L, new BigDecimal("10")));

        assertEquals("Compra rechazada por saldo insuficiente", ex.getMessage());
        assertEquals(EstadoTransaccion.RECHAZADA, ex.getEstado());
        assertEquals(0, tarjeta.getSaldo().compareTo(BigDecimal.ZERO));
        Transaccion guardada = transaccionGuardada();
        assertEquals(EstadoTransaccion.RECHAZADA, guardada.getEstado());
        assertSame(tarjeta, guardada.getTarjeta());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void recarga_tarjetaInexistente_rechaza() {
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.recargar(1L, new BigDecimal("10")));

        assertEquals("Tarjeta no encontrada", ex.getMessage());
    }

    @Test
    void recarga_sumaElSaldoYQuedaExitosa() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "10", LocalDate.now().plusYears(1));
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        Transaccion tx = service.recargar(1L, new BigDecimal("5"));

        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("15")));
        assertEquals(TipoMovimiento.RECARGA, tx.getTipoMovimiento());
        assertEquals(EstadoTransaccion.EXITOSA, tx.getEstado());
        assertEquals(Moneda.USD, tx.getMoneda());
        assertSame(tarjeta, tx.getTarjeta());
        verify(accesoTarjeta).validar(tarjeta);
        verify(tarjetaRepository).save(tarjeta);
        verify(transaccionRepository).save(tx);
    }

    @Test
    void anular_inexistente_rechaza() {
        when(transaccionRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.anularTransaccion(9L));

        assertEquals("No existe la transacción", ex.getMessage());
        verify(accesoTarjeta, never()).validar(any());
    }

    @Test
    void anular_recarga_rechaza() {
        Transaccion tx = compraReciente();
        tx.setTipoMovimiento(TipoMovimiento.RECARGA);
        when(transaccionRepository.findById(9L)).thenReturn(Optional.of(tx));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.anularTransaccion(9L));

        assertEquals("Solo se pueden anular compras", ex.getMessage());
        assertEquals(0, tx.getTarjeta().getSaldo().compareTo(new BigDecimal("20")));
    }

    @Test
    void anular_compraNoExitosa_rechaza() {
        Transaccion tx = compraReciente();
        tx.setEstado(EstadoTransaccion.RECHAZADA);
        when(transaccionRepository.findById(9L)).thenReturn(Optional.of(tx));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.anularTransaccion(9L));

        assertEquals("Solo se pueden anular compras exitosas", ex.getMessage());
    }

    @Test
    void anular_fueraDe24Horas_rechaza() {
        Transaccion tx = compraReciente();
        tx.setFecha(LocalDateTime.now().minusHours(25));
        when(transaccionRepository.findById(9L)).thenReturn(Optional.of(tx));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.anularTransaccion(9L));

        assertEquals("Solo se pueden anular compras menores a 24 horas", ex.getMessage());
        verify(transaccionRepository, never()).save(any());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void anular_compraReciente_devuelveElSaldo() {
        Transaccion tx = compraReciente();
        when(transaccionRepository.findById(9L)).thenReturn(Optional.of(tx));

        Transaccion anulada = service.anularTransaccion(9L);

        assertEquals(EstadoTransaccion.ANULADA, anulada.getEstado());
        assertEquals(0, tx.getTarjeta().getSaldo().compareTo(new BigDecimal("35")));
        verify(accesoTarjeta).validar(tx.getTarjeta());
        verify(transaccionRepository).save(tx);
        verify(tarjetaRepository).save(tx.getTarjeta());
    }

    @Test
    void listar_delegaEnElRepositorio() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "0", LocalDate.now().plusYears(1));
        tarjeta.setId(7L);
        when(tarjetaRepository.findById(7L)).thenReturn(Optional.of(tarjeta));
        when(transaccionRepository.findByTarjetaId(7L)).thenReturn(List.of(new Transaccion()));

        assertEquals(1, service.listarTransaccionesPorTarjeta(7L).size());
        verify(accesoTarjeta).validar(tarjeta);
    }

    @Test
    void listar_tarjetaDeOtroUsuario_rechaza() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "0", LocalDate.now().plusYears(1));
        tarjeta.setId(7L);
        when(tarjetaRepository.findById(7L)).thenReturn(Optional.of(tarjeta));
        doThrow(new BusinessException("Tarjeta no encontrada")).when(accesoTarjeta).validar(tarjeta);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.listarTransaccionesPorTarjeta(7L));

        assertEquals("Tarjeta no encontrada", ex.getMessage());
        verify(transaccionRepository, never()).findByTarjetaId(any());
    }

    @Test
    void listar_incluyeExitosaRechazadaYAnulada() {
        Tarjeta tarjeta = tarjeta(TipoTarjeta.DEBITO, "0", LocalDate.now().plusYears(1));
        tarjeta.setId(7L);
        when(tarjetaRepository.findById(7L)).thenReturn(Optional.of(tarjeta));
        when(transaccionRepository.findByTarjetaId(7L)).thenReturn(List.of(
                transaccionConEstado(EstadoTransaccion.EXITOSA),
                transaccionConEstado(EstadoTransaccion.RECHAZADA),
                transaccionConEstado(EstadoTransaccion.ANULADA)));

        List<EstadoTransaccion> estados = service.listarTransaccionesPorTarjeta(7L).stream()
                .map(Transaccion::getEstado)
                .toList();

        assertEquals(List.of(EstadoTransaccion.EXITOSA, EstadoTransaccion.RECHAZADA, EstadoTransaccion.ANULADA), estados);
    }

    private Transaccion transaccionGuardada() {
        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        return captor.getValue();
    }

    private Transaccion transaccionConEstado(EstadoTransaccion estado) {
        Transaccion tx = new Transaccion();
        tx.setEstado(estado);
        return tx;
    }

    private Transaccion compraReciente() {
        Transaccion tx = new Transaccion();
        tx.setId(9L);
        tx.setTipoMovimiento(TipoMovimiento.COMPRA);
        tx.setEstado(EstadoTransaccion.EXITOSA);
        tx.setFecha(LocalDateTime.now().minusHours(1));
        tx.setMonto(new BigDecimal("15"));
        tx.setTarjeta(tarjeta(TipoTarjeta.DEBITO, "20", LocalDate.now().plusYears(1)));
        return tx;
    }

    private Tarjeta tarjeta(TipoTarjeta tipo, String saldo, LocalDate vencimiento) {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(1L);
        tarjeta.setTipoTarjeta(tipo);
        tarjeta.setSaldo(new BigDecimal(saldo));
        tarjeta.setFechaVencimiento(vencimiento);
        tarjeta.setMoneda(Moneda.USD);
        return tarjeta;
    }
}
