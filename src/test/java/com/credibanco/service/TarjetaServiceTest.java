package com.credibanco.service;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoTarjeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TarjetaServiceTest {

    @Autowired
    private TarjetaService tarjetaService;

    @Test
    void crearRecargarYListar() {
        Tarjeta creada = tarjetaService.crearTarjeta(tarjeta("Maria Garcia", TipoTarjeta.DEBITO));

        assertTrue(creada.getNumeroTarjeta().matches(TipoTarjeta.DEBITO.getIdProducto() + "\\d{10}"));
        assertEquals("Maria Garcia", creada.getNombreTitular());
        assertEquals(Moneda.USD, creada.getMoneda());
        assertEquals(0, creada.getSaldo().compareTo(BigDecimal.ZERO));

        Tarjeta recargada = tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("20000"));
        assertEquals(0, recargada.getSaldo().compareTo(new BigDecimal("20000")));

        Tarjeta listada = tarjetaService.listarTarjetas().stream()
                .filter(tarjeta -> tarjeta.getId().equals(creada.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, listada.getSaldo().compareTo(new BigDecimal("20000")));
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
