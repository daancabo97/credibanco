package com.credibanco.service;

import com.credibanco.models.Tarjeta;
import com.credibanco.models.Transaccion;
import com.credibanco.repository.TarjetaRepository;
import com.credibanco.dto.TarjetaRequest;
import com.credibanco.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarjetaServiceImpl implements TarjetaService {

    private final TarjetaRepository tarjetaRepository;
    private final TransaccionRepository transaccionRepository;

    @Override
    public Tarjeta crearTarjeta(TarjetaRequest tarjetaRequest) {


        Tarjeta tarjeta = new Tarjeta();

        tarjeta.setNumeroTarjeta(tarjetaRequest.getNumeroTarjeta());
        tarjeta.setNombreTitular(tarjetaRequest.getNombreTitular());
        tarjeta.setFechaCreacion(tarjetaRequest.getFechaCreacion());
        tarjeta.setFechaVencimiento(tarjetaRequest.getFechaVencimiento());
        tarjeta.setTipoTarjeta(tarjetaRequest.getTipoTarjeta());

        // Regla: número de 16 dígitos
        if (tarjeta.getNumeroTarjeta() == null || tarjeta.getNumeroTarjeta().length() != 16) {
            throw new IllegalArgumentException("El número de tarjeta debe tener 16 dígitos");
        }
        // Regla: fecha vencimiento >= fecha creación + 3 años (validación opcional)
        if (tarjeta.getFechaVencimiento() == null ||
                tarjeta.getFechaVencimiento().getYear() < LocalDate.now().getYear() + 3) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser al menos 3 años superior a la de creación");
        }
        tarjeta.setSaldo(BigDecimal.ZERO);
        return tarjetaRepository.save(tarjeta);
    }

    @Override
    @Transactional
    public Tarjeta recargarSaldo(Long tarjetaId, BigDecimal monto) {
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
        transaccionRepository.save(tx);

        return tarjeta;
    }

    @Override
    public List<Tarjeta> listarTarjetas() {
        return tarjetaRepository.findAll();
    }
}