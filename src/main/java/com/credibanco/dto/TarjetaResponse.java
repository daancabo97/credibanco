package com.credibanco.dto;

import com.credibanco.models.Moneda;
import com.credibanco.models.TipoTarjeta;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaResponse {

    private Long id;
    private String numeroTarjeta;
    private String nombreTitular;
    private LocalDate fechaCreacion;
    private LocalDate fechaVencimiento;
    private TipoTarjeta tipoTarjeta;
    private BigDecimal saldo;
    private Moneda moneda;
}
