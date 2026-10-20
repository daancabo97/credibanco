package com.credibanco.controller;

import com.credibanco.mapper.ResponseMapper;
import com.credibanco.exception.ApiExceptionHandler;
import com.credibanco.exception.BusinessException;
import com.credibanco.models.EstadoTransaccion;
import com.credibanco.service.TransaccionService;
import com.credibanco.models.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransaccionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransaccionService transaccionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        TransaccionController controller = new TransaccionController(transaccionService, new ResponseMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registrarCompra_OK() throws Exception {
        Transaccion resp = new Transaccion();
        resp.setId(1L);

        Mockito.when(transaccionService.registrarCompra(anyLong(), any(BigDecimal.class))).thenReturn(resp);

        String json = """
        {
          "tarjetaId": 1,
          "monto": 100
        }
        """;

        mockMvc.perform(post("/api/transacciones/compra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tarjeta").doesNotExist());
    }

    @Test
    void registrarCompra_saldoInsuficiente_BadRequest400() throws Exception {
        Mockito.when(transaccionService.registrarCompra(anyLong(), any(BigDecimal.class)))
                .thenThrow(new BusinessException("Saldo insuficiente", EstadoTransaccion.RECHAZADA));

        mockMvc.perform(post("/api/transacciones/compra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tarjetaId": 1, "monto": 100}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Saldo insuficiente"))
                .andExpect(jsonPath("$.tipoError").value("RECHAZADA"));
    }

    @Test
    void recargar_OK() throws Exception {
        Transaccion resp = new Transaccion();
        resp.setId(2L);
        Mockito.when(transaccionService.recargar(eq(1L), eq(new BigDecimal("50")))).thenReturn(resp);

        mockMvc.perform(post("/api/transacciones/recarga")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tarjetaId": 1, "monto": 50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void anular_OK() throws Exception {
        Transaccion resp = new Transaccion();
        resp.setId(4L);
        Mockito.when(transaccionService.anularTransaccion(4L)).thenReturn(resp);

        mockMvc.perform(put("/api/transacciones/4/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void listarPorTarjeta_OK() throws Exception {
        Transaccion resp = new Transaccion();
        resp.setId(9L);
        Mockito.when(transaccionService.listarTransaccionesPorTarjeta(3L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/transacciones").param("tarjetaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9));
    }
}
