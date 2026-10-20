package com.credibanco.dto;

import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Moneda;
import com.credibanco.models.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransaccionResponse {
    private Long id;
    private Long tarjetaId;
    private BigDecimal monto;
    private Moneda moneda;
    private TipoMovimiento tipoMovimiento;
    private EstadoTransaccion estado;
    private LocalDateTime fechaHora;
    private String mensaje;
}
