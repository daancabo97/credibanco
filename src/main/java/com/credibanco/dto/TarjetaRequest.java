package com.credibanco.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaRequest {

    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento;
    private String tipoTarjeta;
}
