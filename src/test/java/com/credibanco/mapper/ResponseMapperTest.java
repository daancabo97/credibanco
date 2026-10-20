package com.credibanco.mapper;

import com.credibanco.dto.TarjetaResponse;
import com.credibanco.dto.TransaccionResponse;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.models.Moneda;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.TipoMovimiento;
import com.credibanco.models.TipoTarjeta;
import com.credibanco.models.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseMapperTest {

    private ResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResponseMapper();
    }

    @Test
    void tarjetaCreada_conservaElNumeroCompleto() {
        TarjetaResponse response = mapper.toTarjetaCreadaResponse(tarjeta("1234567890123456"));

        assertEquals("1234567890123456", response.getNumeroTarjeta());
        assertEquals(8L, response.getId());
        assertEquals("Ana Lopez", response.getNombreTitular());
        assertEquals(LocalDate.of(2020, 1, 1), response.getFechaCreacion());
        assertEquals(LocalDate.of(2028, 1, 1), response.getFechaVencimiento());
        assertEquals(TipoTarjeta.CREDITO, response.getTipoTarjeta());
        assertEquals(0, response.getSaldo().compareTo(new BigDecimal("40")));
        assertEquals(Moneda.USD, response.getMoneda());
    }

    @Test
    void tarjetaEnmascarada_ocultaElCentroDelNumero() {
        Tarjeta tarjeta = tarjeta("1234567890123456");

        assertEquals("123456******3456", mapper.toTarjetaEnmascarada(tarjeta).getNumeroTarjeta());
    }

    @Test
    void numeroNuloOIncompleto_noSeEnmascara() {
        Tarjeta corta = tarjeta("12345");
        assertEquals("12345", mapper.toTarjetaEnmascarada(corta).getNumeroTarjeta());

        Tarjeta sinNumero = tarjeta(null);
        assertNull(mapper.toTarjetaEnmascarada(sinNumero).getNumeroTarjeta());
    }

    @Test
    void listaDeTarjetas_mapeaCadaElemento() {
        List<TarjetaResponse> respuestas = mapper.toTarjetaEnmascarada(List.of(
                tarjeta("1234567890123456"),
                tarjeta("6543217890123456")));

        assertEquals(2, respuestas.size());
        assertEquals("654321******3456", respuestas.get(1).getNumeroTarjeta());
    }

    @Test
    void transaccionSinTarjeta_dejaElIdNulo() {
        Transaccion tx = transaccion();

        TransaccionResponse response = mapper.toTransaccionResponse(tx);

        assertNull(response.getTarjetaId());
        assertEquals(3L, response.getId());
        assertEquals(0, response.getMonto().compareTo(new BigDecimal("15")));
        assertEquals(Moneda.USD, response.getMoneda());
        assertEquals(TipoMovimiento.COMPRA, response.getTipoMovimiento());
        assertEquals(EstadoTransaccion.EXITOSA, response.getEstado());
        assertEquals("Compra exitosa", response.getMensaje());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 30), response.getFechaHora());
    }

    @Test
    void transaccionConTarjeta_copiaSuId() {
        Transaccion tx = transaccion();
        Tarjeta tarjeta = tarjeta("1234567890123456");
        tarjeta.setId(4L);
        tx.setTarjeta(tarjeta);

        assertEquals(4L, mapper.toTransaccionResponse(tx).getTarjetaId());
    }

    @Test
    void transaccionExitosaOAnulada_incluyeMensaje() {
        Transaccion recarga = transaccion();
        recarga.setTipoMovimiento(TipoMovimiento.RECARGA);
        assertEquals("Recarga exitosa", mapper.toTransaccionResponse(recarga).getMensaje());

        Transaccion anulada = transaccion();
        anulada.setEstado(EstadoTransaccion.ANULADA);
        assertEquals("Transacción anulada exitosamente", mapper.toTransaccionResponse(anulada).getMensaje());
    }

    @Test
    void listaDeTransacciones_mapeaCadaElemento() {
        List<TransaccionResponse> respuestas = mapper.toTransaccionResponseList(List.of(transaccion()));

        assertEquals(1, respuestas.size());
        assertEquals(3L, respuestas.get(0).getId());
    }

    private Tarjeta tarjeta(String numero) {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(8L);
        tarjeta.setNumeroTarjeta(numero);
        tarjeta.setNombreTitular("Ana Lopez");
        tarjeta.setFechaCreacion(LocalDate.of(2020, 1, 1));
        tarjeta.setFechaVencimiento(LocalDate.of(2028, 1, 1));
        tarjeta.setTipoTarjeta(TipoTarjeta.CREDITO);
        tarjeta.setSaldo(new BigDecimal("40"));
        tarjeta.setMoneda(Moneda.USD);
        return tarjeta;
    }

    private Transaccion transaccion() {
        Transaccion tx = new Transaccion();
        tx.setId(3L);
        tx.setMonto(new BigDecimal("15"));
        tx.setMoneda(Moneda.USD);
        tx.setTipoMovimiento(TipoMovimiento.COMPRA);
        tx.setEstado(EstadoTransaccion.EXITOSA);
        tx.setFecha(LocalDateTime.of(2026, 5, 1, 10, 30));
        return tx;
    }
}
