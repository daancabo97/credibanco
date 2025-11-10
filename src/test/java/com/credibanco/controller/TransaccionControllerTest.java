package com.credibanco.controller;

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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransaccionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransaccionService transaccionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TransaccionController controller = new TransaccionController(transaccionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
                        .param("tarjetaId", "1")
                        .param("monto", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
