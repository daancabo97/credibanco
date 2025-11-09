package com.credibanco.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaResponse {

    private Long id;
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento;
    private String tipoTarjeta;
    private BigDecimal saldo;
}
