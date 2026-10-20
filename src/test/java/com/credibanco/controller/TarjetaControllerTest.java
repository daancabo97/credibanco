package com.credibanco.controller;

import com.credibanco.exception.ApiExceptionHandler;
import com.credibanco.mapper.ResponseMapper;
import com.credibanco.service.TarjetaService;
import com.credibanco.dto.TarjetaRequest;
import com.credibanco.models.Tarjeta;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TarjetaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TarjetaService tarjetaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        TarjetaController controller = new TarjetaController(tarjetaService, new ResponseMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void crearTarjeta_OK() throws Exception {
        Tarjeta resp = new Tarjeta();
        resp.setId(1L);
        resp.setNumeroTarjeta("1234567890123456");

        Mockito.when(tarjetaService.crearTarjeta(any(TarjetaRequest.class))).thenReturn(resp);

        String json = """
        {
          "nombreTitular": "Juan Perez",
          "fechaCreacion": "2026-01-01",
          "fechaVencimiento": "2030-01-01",
          "tipoTarjeta": "CREDITO"
        }
        """;

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroTarjeta").value("1234567890123456"))
                .andExpect(jsonPath("$.transacciones").doesNotExist());
    }

    @Test
    void crearTarjeta_sinTitular_BadRequest400() throws Exception {
        String json = """
        {
          "fechaCreacion": "2026-01-01",
          "fechaVencimiento": "2030-01-01",
          "tipoTarjeta": "CREDITO"
        }
        """;

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipoError").value("Validacion"))
                .andExpect(jsonPath("$.mensaje").value("El nombre del titular es obligatorio"));
    }

    @Test
    void listarTarjetas_enmascaraNumero() throws Exception {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(1L);
        tarjeta.setNumeroTarjeta("1234567890123456");
        Mockito.when(tarjetaService.listarTarjetas()).thenReturn(List.of(tarjeta));

        mockMvc.perform(get("/api/tarjetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroTarjeta").value("123456******3456"));
    }

    @Test
    void recargar_enmascaraNumero() throws Exception {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(1L);
        tarjeta.setNumeroTarjeta("1234567890123456");
        Mockito.when(tarjetaService.recargarSaldo(eq(1L), eq(new BigDecimal("100")))).thenReturn(tarjeta);

        mockMvc.perform(put("/api/tarjetas/1/recargar").param("monto", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTarjeta").value("123456******3456"));
    }
}
