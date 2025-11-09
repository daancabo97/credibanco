package com.credibanco.service;

import com.credibanco.models.Tarjeta;
import java.math.BigDecimal;
import java.util.List;

public interface TarjetaService {
    Tarjeta crearTarjeta(Tarjeta tarjeta);
    void recargarSaldo(Long tarjetaId, BigDecimal monto);
    List<Tarjeta> listarTarjetas();
}
