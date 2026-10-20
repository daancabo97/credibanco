package com.credibanco.service;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.Moneda;
import com.credibanco.models.TipoTarjeta;
import com.credibanco.models.Tarjeta;
import com.credibanco.repository.TarjetaRepository;
import com.credibanco.dto.TarjetaRequest;
import com.credibanco.models.Usuario;
import com.credibanco.security.AccesoTarjeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
public class TarjetaServiceImpl implements TarjetaService {

    private static final Logger log = LoggerFactory.getLogger(TarjetaServiceImpl.class);
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final TarjetaRepository tarjetaRepository;
    private final TransaccionService transaccionService;
    private final AccesoTarjeta accesoTarjeta;

    public TarjetaServiceImpl(TarjetaRepository tarjetaRepository,
                              TransaccionService transaccionService,
                              AccesoTarjeta accesoTarjeta) {
        this.tarjetaRepository = tarjetaRepository;
        this.transaccionService = transaccionService;
        this.accesoTarjeta = accesoTarjeta;
    }

    @Override
    public Tarjeta crearTarjeta(TarjetaRequest tarjetaRequest) {


        Tarjeta tarjeta = new Tarjeta();

        if (tarjetaRequest.getTipoTarjeta() == null) {
            throw new BusinessException("El tipo de tarjeta es obligatorio");
        }
        tarjeta.setNombreTitular(nombreTitularValido(tarjetaRequest.getNombreTitular()));
        tarjeta.setNumeroTarjeta(generarNumeroTarjeta(tarjetaRequest.getTipoTarjeta()));
        tarjeta.setFechaCreacion(tarjetaRequest.getFechaCreacion());
        tarjeta.setFechaVencimiento(tarjetaRequest.getFechaVencimiento());
        tarjeta.setTipoTarjeta(tarjetaRequest.getTipoTarjeta());
        LocalDate creacion = tarjeta.getFechaCreacion();
        LocalDate vencimiento = tarjeta.getFechaVencimiento();
        if (creacion == null || vencimiento == null || vencimiento.isBefore(creacion.plusYears(3))) {
            throw new BusinessException("La fecha de vencimiento debe ser al menos 3 años superior a la de creación");
        }
        tarjeta.setSaldo(BigDecimal.ZERO);
        tarjeta.setMoneda(Moneda.USD);
        tarjeta.setUsuario(accesoTarjeta.usuarioActual());
        tarjetaRepository.save(tarjeta);
        log.info("Tarjeta creada: id={}, tipo={}", tarjeta.getId(), tarjeta.getTipoTarjeta());
        return tarjeta;
    }

    @Override
    @Transactional
    public Tarjeta recargarSaldo(Long tarjetaId, BigDecimal monto) {
        return transaccionService.recargar(tarjetaId, monto).getTarjeta();
    }

    @Override
    public List<Tarjeta> listarTarjetas() {
        Usuario actual = accesoTarjeta.usuarioActual();
        if (actual == null) {
            return tarjetaRepository.findAll();
        }
        return tarjetaRepository.findByUsuarioId(actual.getId());
    }

    private String nombreTitularValido(String nombre) {
        String[] PartsName = nombre == null ? new String[0] : nombre.trim().split("\\s+");
        if (nombre == null || nombre.isBlank() || PartsName.length < 2) {
            throw new BusinessException("El nombre del titular debe incluir primer nombre y apellido");
        }
        return String.join(" ", PartsName);
    }

    private String generarNumeroTarjeta(TipoTarjeta tipo) {
        String idProducto = tipo.getIdProducto();
        for (int intento = 0; intento < 5; intento++) {
            String numero = idProducto + digitosAleatorios(10);
            if (tarjetaRepository.findByNumeroTarjeta(numero).isEmpty()) {
                return numero;
            }
        }
        throw new BusinessException("No fue posible generar un número de tarjeta único");
    }

    private String digitosAleatorios(int cantidad) {
        StringBuilder digitos = new StringBuilder(cantidad);
        for (int i = 0; i < cantidad; i++) {
            digitos.append(ALEATORIO.nextInt(10));
        }
        return digitos.toString();
    }
}