package com.credibanco.service;

import com.credibanco.models.Tarjeta;
import com.credibanco.models.Transaccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransaccionServiceIntegrationTest {

    @Autowired
    private TarjetaService tarjetaService;

    @Autowired
    private TransaccionService transaccionService;


    @Test
    public void testCompraYAnulacion() {
        // Crear tarjeta
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta("9999888877776666");
        tarjeta.setNombreTitular("Luis Diaz");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento("112028");
        tarjeta.setTipoTarjeta("debito");
        tarjeta.setSaldo(new BigDecimal("50000"));
        Tarjeta creada = tarjetaService.crearTarjeta(tarjeta);

        // Realizar compra
        Transaccion txCompra = transaccionService.registrarCompra(creada.getId(), new BigDecimal("10000"));
        assertEquals("exitosa", txCompra.getEstado());

        // Anular compra
        Transaccion txAnulada = transaccionService.anularTransaccion(txCompra.getId());
        assertEquals("anulada", txAnulada.getEstado());

        // Validar saldo reintegrado
        Tarjeta actual = tarjetaService.listarTarjetas().stream()
                .filter(t -> t.getId().equals(creada.getId())).findFirst().orElse(null);
        assertEquals(new BigDecimal("50000"), actual.getSaldo());
    }

    @Test
    public void testConsultarTransaccionesPorTarjeta() {
        // Transacciones creadas por tarjeta
        Long tarjetaId = 1L;
        List<Transaccion> transacciones = transaccionService.listarTransaccionesPorTarjeta(tarjetaId);
        assertNotNull(transacciones);
        assertFalse(transacciones.isEmpty());
    }
}
