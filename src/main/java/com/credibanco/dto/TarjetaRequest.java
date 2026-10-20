package com.credibanco.dto;

import com.credibanco.models.TipoTarjeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaRequest {

    @NotBlank(message = "El nombre del titular es obligatorio")
    private String nombreTitular;

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDate fechaCreacion;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El tipo de tarjeta es obligatorio")
    private TipoTarjeta tipoTarjeta;
}
