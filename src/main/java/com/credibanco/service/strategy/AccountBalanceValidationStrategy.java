package com.credibanco.service.strategy;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Tarjeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountBalanceValidationStrategy implements AccountValidationStrategy {

    private static final Logger log = LoggerFactory.getLogger(AccountBalanceValidationStrategy.class);
    private static final String ERROR_MESSAGE = "Compra rechazada por saldo insuficiente";

    @Override
    public void validateProcessTransaction(Tarjeta tarjeta, BigDecimal amount) {
        if (tarjeta.getSaldo().compareTo(amount) < 0) {
            log.warn("Compra rechazada por saldo insuficiente: tarjeta={}, monto={}", tarjeta.getId(), amount);
            throw new BusinessException(ERROR_MESSAGE, EstadoTransaccion.RECHAZADA);
        }
        tarjeta.setSaldo(tarjeta.getSaldo().subtract(amount));
    }
}
