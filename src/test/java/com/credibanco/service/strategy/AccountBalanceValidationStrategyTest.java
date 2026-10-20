package com.credibanco.service.strategy;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Tarjeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountBalanceValidationStrategyTest {

    private AccountBalanceValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AccountBalanceValidationStrategy();
    }

    @Test
    void descuentaCuandoHaySaldo() {
        Tarjeta tarjeta = tarjeta("100");

        strategy.validateProcessTransaction(tarjeta, new BigDecimal("40"));

        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("60")));
    }

    @Test
    void permiteMontoIgualAlSaldo() {
        Tarjeta tarjeta = tarjeta("80");

        strategy.validateProcessTransaction(tarjeta, new BigDecimal("80"));

        assertEquals(0, tarjeta.getSaldo().compareTo(BigDecimal.ZERO));
    }

    @Test
    void rechazaCuandoElSaldoNoAlcanza() {
        Tarjeta tarjeta = tarjeta("100");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> strategy.validateProcessTransaction(tarjeta, new BigDecimal("100.01")));

        assertEquals("Compra rechazada por saldo insuficiente", ex.getMessage());
        assertEquals(EstadoTransaccion.RECHAZADA, ex.getEstado());
        assertEquals(0, tarjeta.getSaldo().compareTo(new BigDecimal("100")));
    }

    private Tarjeta tarjeta(String saldo) {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setSaldo(new BigDecimal(saldo));
        return tarjeta;
    }
}
