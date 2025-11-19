package com.credibanco.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class TransaccionResponse {
    private Long id;
    private Long tarjetaId;
    private BigDecimal monto;
    private String tipoMovimiento;
    private String estado;
    private LocalDate fechaHora;
}
