package com.credibanco.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaRequest {

    private String numeroTarjeta;
    private String nombreTitular;
    private LocalDate fechaCreacion;
    private LocalDate fechaVencimiento;
    private String tipoTarjeta;
}
