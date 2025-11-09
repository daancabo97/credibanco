package com.credibanco.service;

import com.credibanco.models.Transaccion;
import java.math.BigDecimal;
import java.util.List;

public interface TransaccionService {
    Transaccion registrarCompra(Long tarjetaId, BigDecimal monto);
    Transaccion recargar(Long tarjetaId, BigDecimal monto);
    Transaccion anularTransaccion(Long transaccionId);
    List<Transaccion> listarTransaccionesPorTarjeta(Long tarjetaId);
}

