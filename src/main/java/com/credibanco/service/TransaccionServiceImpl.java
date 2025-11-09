package com.credibanco.service;

import com.credibanco.models.Transaccion;
import com.credibanco.models.Tarjeta;
import com.credibanco.repository.TransaccionRepository;
import com.credibanco.repository.TarjetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {
    private final TransaccionRepository transaccionRepository;
    private final TarjetaRepository tarjetaRepository;

    public TransaccionServiceImpl(TransaccionRepository transaccionRepository, TarjetaRepository tarjetaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    @Override
    @Transactional
    public Transaccion registrarCompra(Long tarjetaId, BigDecimal monto) {
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
        if ("debito".equalsIgnoreCase(tarjeta.getTipoTarjeta()) && tarjeta.getSaldo().compareTo(monto) < 0)
            throw new IllegalArgumentException("Saldo insuficiente");
        Transaccion tx = new Transaccion();
        tx.setTarjeta(tarjeta);
        tx.setMonto(monto);
        tx.setFecha(LocalDateTime.now());
        tx.setTipoMovimiento("compra");
        tx.setEstado("exitosa");
        transaccionRepository.save(tx);
        if ("debito".equalsIgnoreCase(tarjeta.getTipoTarjeta())) {
            tarjeta.setSaldo(tarjeta.getSaldo().subtract(monto));
            tarjetaRepository.save(tarjeta);
        }
        return tx;
    }

    @Override
    @Transactional
    public Transaccion recargar(Long tarjetaId, BigDecimal monto) {
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
        tarjeta.setSaldo(tarjeta.getSaldo().add(monto));
        tarjetaRepository.save(tarjeta);
        Transaccion tx = new Transaccion();
        tx.setTarjeta(tarjeta);
        tx.setMonto(monto);
        tx.setFecha(LocalDateTime.now());
        tx.setTipoMovimiento("recarga");
        tx.setEstado("exitosa");
        return transaccionRepository.save(tx);
    }

    @Override
    @Transactional
    public Transaccion anularTransaccion(Long transaccionId) {
        Transaccion tx = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la transacción"));
        if (!"compra".equalsIgnoreCase(tx.getTipoMovimiento()))
            throw new IllegalArgumentException("Solo se pueden anular compras");
        if (!"exitosa".equalsIgnoreCase(tx.getEstado()))
            throw new IllegalArgumentException("Solo se pueden anular compras exitosas");
        if (tx.getFecha().isBefore(LocalDateTime.now().minusHours(24)))
            throw new IllegalArgumentException("Solo se pueden anular compras menores a 24 horas");
        tx.setEstado("anulada");
        transaccionRepository.save(tx);
        Tarjeta tarjeta = tx.getTarjeta();
        tarjeta.setSaldo(tarjeta.getSaldo().add(tx.getMonto()));
        tarjetaRepository.save(tarjeta);
        return tx;
    }

    @Override
    public List<Transaccion> listarTransaccionesPorTarjeta(Long tarjetaId) { return transaccionRepository.findByTarjetaId(tarjetaId); }
}
