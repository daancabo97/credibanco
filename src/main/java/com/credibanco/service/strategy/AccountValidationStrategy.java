package com.credibanco.service.strategy;

import com.credibanco.models.Tarjeta;

import java.math.BigDecimal;

public interface AccountValidationStrategy {

    void validateProcessTransaction(Tarjeta tarjeta, BigDecimal amount);
}
