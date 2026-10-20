package com.credibanco.exception;

import com.credibanco.models.EstadoTransaccion;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BusinessException extends RuntimeException {

    private EstadoTransaccion estado;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, EstadoTransaccion estado) {
        super(message);
        this.estado = estado;
    }
}
