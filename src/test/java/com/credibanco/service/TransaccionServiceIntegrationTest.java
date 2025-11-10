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
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(generarNumeroTarjetaUnico());
        tarjeta.setNombreTitular("Luis Diaz");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento("112028");
        tarjeta.setTipoTarjeta("debito");
        Tarjeta creada = tarjetaService.crearTarjeta(tarjeta);

        // Recargar antes de comprar
        tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("50000"));

        // Realizar compra de 10000 que debe ser exitosa
        Transaccion txCompra = transaccionService.registrarCompra(creada.getId(), new BigDecimal("10000"));
        assertEquals("exitosa", txCompra.getEstado());

        // Anular la compra
        Transaccion txAnulada = transaccionService.anularTransaccion(txCompra.getId());
        assertEquals("anulada", txAnulada.getEstado());

        // Validar que el saldo está restaurado a 50,000
        Tarjeta actual = tarjetaService.listarTarjetas().stream()
                .filter(t -> t.getId().equals(creada.getId()))
                .findFirst().orElse(null);
        assertTrue(actual.getSaldo().compareTo(new BigDecimal("50000")) == 0);
    }

    @Test
    public void testConsultarTransaccionesPorTarjeta() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(generarNumeroTarjetaUnico());
        tarjeta.setNombreTitular("Carlos Perez");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento("112028");
        tarjeta.setTipoTarjeta("credito");
        Tarjeta creada = tarjetaService.crearTarjeta(tarjeta);

        // Recargar antes de comprar
        tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("70000"));

        // Registrar una compra
        transaccionService.registrarCompra(creada.getId(), new BigDecimal("20000"));
        // Consultar transacciones
        List<Transaccion> transacciones = transaccionService.listarTransaccionesPorTarjeta(creada.getId());

        assertNotNull(transacciones);
        assertFalse(transacciones.isEmpty());
    }

        private String generarNumeroTarjetaUnico() {
            String base = "999988887777";
            String sufijo = String.valueOf(System.currentTimeMillis());
            return base + sufijo.substring(sufijo.length() - 4);

    }
}
