package com.credibanco.service;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoTarjeta;
import com.credibanco.models.Transaccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransaccionServiceTest {

    @Autowired
    private TarjetaService tarjetaService;

    @Autowired
    private TransaccionService transaccionService;

    @Test
    void compraAnulacionYRechazoQuedanEnElListado() {
        Tarjeta creada = tarjetaService.crearTarjeta(tarjeta("Luis Diaz", TipoTarjeta.DEBITO));
        assertTrue(creada.getNumeroTarjeta().matches(TipoTarjeta.DEBITO.getIdProducto() + "\\d{10}"));
        assertEquals(Moneda.USD, creada.getMoneda());

        tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("50000"));
        Transaccion compra = transaccionService.registrarCompra(creada.getId(), new BigDecimal("10000"));
        assertEquals(EstadoTransaccion.EXITOSA, compra.getEstado());
        assertEquals(Moneda.USD, compra.getMoneda());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> transaccionService.registrarCompra(creada.getId(), new BigDecimal("999999")));
        assertEquals(EstadoTransaccion.RECHAZADA, ex.getEstado());

        Transaccion anulada = transaccionService.anularTransaccion(compra.getId());
        assertEquals(EstadoTransaccion.ANULADA, anulada.getEstado());

        List<Transaccion> transacciones = transaccionService.listarTransaccionesPorTarjeta(creada.getId());
        assertEquals(3, transacciones.size());
        assertEquals(
                Set.of(EstadoTransaccion.EXITOSA, EstadoTransaccion.RECHAZADA, EstadoTransaccion.ANULADA),
                transacciones.stream().map(Transaccion::getEstado).collect(Collectors.toSet()));

        Tarjeta actual = tarjetaService.listarTarjetas().stream()
                .filter(tarjeta -> tarjeta.getId().equals(creada.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, actual.getSaldo().compareTo(new BigDecimal("50000")));
    }

    private TarjetaRequest tarjeta(String nombreTitular, TipoTarjeta tipoTarjeta) {
        TarjetaRequest request = new TarjetaRequest();
        request.setNombreTitular(nombreTitular);
        request.setFechaCreacion(LocalDate.now());
        request.setFechaVencimiento(LocalDate.now().plusYears(4));
        request.setTipoTarjeta(tipoTarjeta);
        return request;
    }
}
