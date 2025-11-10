package com.credibanco.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TransaccionRequest {
    private Long tarjetaId;
    private BigDecimal monto;
    private String tipoMovimiento; // "compra" "recarga"
}