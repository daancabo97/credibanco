package com.credibanco.repository;

import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoTarjeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TarjetaRepositoryTest {

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Test
    void guardaConSaldoCeroYLaEncuentraPorNumero() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta("1234567890123456");
        tarjeta.setNombreTitular("Daniel Caicedo");
        tarjeta.setFechaCreacion(LocalDate.now());
        tarjeta.setFechaVencimiento(LocalDate.of(2058, 11, 1));
        tarjeta.setTipoTarjeta(TipoTarjeta.CREDITO);

        Long id = tarjetaRepository.save(tarjeta).getId();
        Tarjeta encontrada = tarjetaRepository.findByNumeroTarjeta("1234567890123456").orElseThrow();

        assertEquals(id, encontrada.getId());
        assertEquals(0, encontrada.getSaldo().compareTo(BigDecimal.ZERO));
        assertEquals(Moneda.USD, encontrada.getMoneda());
    }
}
