package com.credibanco.service;

import com.credibanco.models.Tarjeta;
import com.credibanco.repository.TarjetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TarjetaServiceTest {

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Test
    public void testCrearTarjeta() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta("1234567890123456");
        tarjeta.setNombreTitular("Juan Perez");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento("112028");
        tarjeta.setTipoTarjeta("credito");
        tarjeta.setSaldo(new java.math.BigDecimal("0"));
        Tarjeta saved = tarjetaRepository.save(tarjeta);

        assertNotNull(saved.getId());
        assertEquals("Juan Perez", saved.getNombreTitular());
    }

    @Test
    public void testRecargarSaldo() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta("5555123412341234");
        tarjeta.setNombreTitular("Maria Garcia");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento("112028");
        tarjeta.setTipoTarjeta("debito");
        tarjeta.setSaldo(new java.math.BigDecimal("50000"));
        Tarjeta saved = tarjetaRepository.save(tarjeta);

        saved.setSaldo(saved.getSaldo().add(new java.math.BigDecimal("20000")));
        tarjetaRepository.save(saved);

        Tarjeta reloaded = tarjetaRepository.findById(saved.getId()).orElse(null);
        assertEquals(new java.math.BigDecimal("70000"), reloaded.getSaldo());
    }

}
