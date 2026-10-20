package com.credibanco.mapper;

import com.credibanco.dto.TarjetaResponse;
import com.credibanco.dto.TransaccionResponse;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoMovimiento;
import com.credibanco.models.Transaccion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseMapper {

    public TarjetaResponse toTarjetaCreadaResponse(Tarjeta tarjeta) {
        return mapTarjeta(tarjeta, tarjeta.getNumeroTarjeta());
    }

    public TarjetaResponse toTarjetaEnmascarada(Tarjeta tarjeta) {
        return mapTarjeta(tarjeta, enmascararNumero(tarjeta.getNumeroTarjeta()));
    }

    public List<TarjetaResponse> toTarjetaEnmascarada(List<Tarjeta> tarjetas) {
        return tarjetas.stream().map(this::toTarjetaEnmascarada).toList();
    }

    private TarjetaResponse mapTarjeta(Tarjeta tarjeta, String numeroTarjeta) {
        TarjetaResponse response = new TarjetaResponse();
        response.setId(tarjeta.getId());
        response.setNumeroTarjeta(numeroTarjeta);
        response.setNombreTitular(tarjeta.getNombreTitular());
        response.setFechaCreacion(tarjeta.getFechaCreacion());
        response.setFechaVencimiento(tarjeta.getFechaVencimiento());
        response.setTipoTarjeta(tarjeta.getTipoTarjeta());
        response.setSaldo(tarjeta.getSaldo());
        response.setMoneda(tarjeta.getMoneda());
        return response;
    }

    private String enmascararNumero(String numero) {
        if (numero == null || numero.length() != 16) {
            return numero;
        }
        return numero.substring(0, 6) + "******" + numero.substring(12);
    }

    public TransaccionResponse toTransaccionResponse(Transaccion transaccion) {
        TransaccionResponse response = new TransaccionResponse();
        response.setId(transaccion.getId());
        response.setTarjetaId(transaccion.getTarjeta() == null ? null : transaccion.getTarjeta().getId());
        response.setMonto(transaccion.getMonto());
        response.setMoneda(transaccion.getMoneda());
        response.setTipoMovimiento(transaccion.getTipoMovimiento());
        response.setEstado(transaccion.getEstado());
        response.setFechaHora(transaccion.getFecha());
        response.setMensaje(mensaje(transaccion));
        return response;
    }

    private String mensaje(Transaccion transaccion) {
        if (transaccion.getEstado() == EstadoTransaccion.EXITOSA && transaccion.getTipoMovimiento() == TipoMovimiento.COMPRA) {
            return "Compra exitosa";
        }
        if (transaccion.getEstado() == EstadoTransaccion.EXITOSA && transaccion.getTipoMovimiento() == TipoMovimiento.RECARGA) {
            return "Recarga exitosa";
        }
        if (transaccion.getEstado() == EstadoTransaccion.ANULADA) {
            return "Transacción anulada exitosamente";
        }
        return null;
    }

    public List<TransaccionResponse> toTransaccionResponseList(List<Transaccion> transacciones) {
        return transacciones.stream().map(this::toTransaccionResponse).toList();
    }
}
