package com.credibanco.service;

import com.credibanco.models.Tarjeta;
import com.credibanco.repository.TarjetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
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
        tarjeta.setNombreTitular("Daniel Caicedo");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento(LocalDate.of(2058, 11, 1));
        tarjeta.setTipoTarjeta("credito");
        // saldo NO se asigna, ya que la clase lo inicializa en cero

        Tarjeta saved = tarjetaRepository.save(tarjeta);

        // Debe ser cero el saldo siempre que se cree una tarjeta
        assertNotNull(saved.getId());
        assertEquals("Daniel Caicedo", saved.getNombreTitular());
        assertTrue(saved.getSaldo().compareTo(new BigDecimal("0")) == 0);
    }

    @Test
    public void testRecargarSaldo() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta("5555123412341234");
        tarjeta.setNombreTitular("Maria Garcia");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento(LocalDate.of(2058, 11, 1));
        tarjeta.setTipoTarjeta("debito");

        Tarjeta saved = tarjetaRepository.save(tarjeta);

        // Simular recarga de saldo
        saved.setSaldo(saved.getSaldo().add(new java.math.BigDecimal("20000")));
        tarjetaRepository.save(saved);

        Tarjeta reloaded = tarjetaRepository.findById(saved.getId()).orElse(null);
        assertTrue(reloaded.getSaldo().compareTo(new BigDecimal("20000")) == 0);
    }

}
