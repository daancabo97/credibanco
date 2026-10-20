package com.credibanco.service;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Moneda;
import com.credibanco.models.TipoMovimiento;
import com.credibanco.models.Transaccion;
import com.credibanco.models.Tarjeta;
import com.credibanco.repository.TransaccionRepository;
import com.credibanco.repository.TarjetaRepository;
import com.credibanco.security.AccesoTarjeta;
import com.credibanco.service.strategy.AccountValidationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionServiceImpl.class);

    private final TransaccionRepository transaccionRepository;
    private final TarjetaRepository tarjetaRepository;
    private final AccesoTarjeta accesoTarjeta;
    private final AccountValidationStrategy balanceStrategy;

    public TransaccionServiceImpl(TransaccionRepository transaccionRepository,
                                  TarjetaRepository tarjetaRepository,
                                  AccesoTarjeta accesoTarjeta,
                                  AccountValidationStrategy balanceStrategy) {
        this.transaccionRepository = transaccionRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.accesoTarjeta = accesoTarjeta;
        this.balanceStrategy = balanceStrategy;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public Transaccion registrarCompra(Long tarjetaId, BigDecimal monto) {
        validarMonto(monto);
        Tarjeta tarjeta = buscarTarjeta(tarjetaId);
        if (tarjeta.getFechaVencimiento() != null && tarjeta.getFechaVencimiento().isBefore(LocalDate.now())) {
            log.warn("Compra rechazada por tarjeta vencida: tarjeta={}", tarjetaId);
            registrarRechazo(tarjeta, monto);
            throw new BusinessException("Tarjeta vencida", EstadoTransaccion.RECHAZADA);
        }
        try {
            balanceStrategy.validateProcessTransaction(tarjeta, monto);
        } catch (BusinessException ex) {
            if (ex.getEstado() == EstadoTransaccion.RECHAZADA) {
                registrarRechazo(tarjeta, monto);
            }
            throw ex;
        }
        Transaccion tx = registrarMovimiento(tarjeta, monto, TipoMovimiento.COMPRA);
        log.info("Compra registrada: tarjeta={}, monto={}, txId={}", tarjetaId, monto, tx.getId());
        return tx;
    }

    @Override
    @Transactional
    public Transaccion recargar(Long tarjetaId, BigDecimal monto) {
        validarMonto(monto);
        Tarjeta tarjeta = buscarTarjeta(tarjetaId);
        tarjeta.setSaldo(tarjeta.getSaldo().add(monto));
        Transaccion tx = registrarMovimiento(tarjeta, monto, TipoMovimiento.RECARGA);
        log.info("Recarga registrada: tarjeta={}, monto={}, txId={}", tarjetaId, monto, tx.getId());
        return tx;
    }

    private Tarjeta buscarTarjeta(Long tarjetaId) {
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new BusinessException("Tarjeta no encontrada"));
        accesoTarjeta.validar(tarjeta);
        return tarjeta;
    }

    private Transaccion registrarMovimiento(Tarjeta tarjeta, BigDecimal monto, TipoMovimiento tipo) {
        Transaccion tx = new Transaccion();
        tx.setTarjeta(tarjeta);
        tx.setMonto(monto);
        tx.setMoneda(Moneda.USD);
        tx.setFecha(LocalDateTime.now());
        tx.setTipoMovimiento(tipo);
        tx.setEstado(EstadoTransaccion.EXITOSA);
        transaccionRepository.save(tx);
        tarjetaRepository.save(tarjeta);
        return tx;
    }

    private void registrarRechazo(Tarjeta tarjeta, BigDecimal monto) {
        Transaccion tx = new Transaccion();
        tx.setTarjeta(tarjeta);
        tx.setMonto(monto);
        tx.setMoneda(Moneda.USD);
        tx.setFecha(LocalDateTime.now());
        tx.setTipoMovimiento(TipoMovimiento.COMPRA);
        tx.setEstado(EstadoTransaccion.RECHAZADA);
        transaccionRepository.save(tx);
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto debe ser mayor que cero");
        }
    }

    @Override
    @Transactional
    public Transaccion anularTransaccion(Long transaccionId) {
        Transaccion tx = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new BusinessException("No existe la transacción"));
        accesoTarjeta.validar(tx.getTarjeta());
        if (tx.getTipoMovimiento() != TipoMovimiento.COMPRA)
            throw new BusinessException("Solo se pueden anular compras");
        if (tx.getEstado() != EstadoTransaccion.EXITOSA)
            throw new BusinessException("Solo se pueden anular compras exitosas");
        if (tx.getFecha().isBefore(LocalDateTime.now().minusHours(24))) {
            log.warn("Intento de anulación fuera de plazo: txId={}", transaccionId);
            throw new BusinessException("Solo se pueden anular compras menores a 24 horas");
        }
        tx.setEstado(EstadoTransaccion.ANULADA);
        transaccionRepository.save(tx);
        Tarjeta tarjeta = tx.getTarjeta();
        tarjeta.setSaldo(tarjeta.getSaldo().add(tx.getMonto()));
        tarjetaRepository.save(tarjeta);
        log.info("Transacción anulada: txId={}, monto={}", transaccionId, tx.getMonto());
        return tx;
    }

    @Override
    public List<Transaccion> listarTransaccionesPorTarjeta(Long tarjetaId) {
        buscarTarjeta(tarjetaId);
        return transaccionRepository.findByTarjetaId(tarjetaId);
    }
}
