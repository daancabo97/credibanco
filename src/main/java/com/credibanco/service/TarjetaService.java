package com.credibanco.service;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.models.Tarjeta;
import java.math.BigDecimal;
import java.util.List;

public interface TarjetaService {
    Tarjeta crearTarjeta(TarjetaRequest tarjetaRequest);
    Tarjeta recargarSaldo(Long tarjetaId, BigDecimal monto);
    List<Tarjeta> listarTarjetas();
}
