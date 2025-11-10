package com.credibanco.controller;

import com.credibanco.service.TarjetaService;
import com.credibanco.models.Tarjeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TarjetaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TarjetaService tarjetaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los @Mock
        TarjetaController controller = new TarjetaController(tarjetaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void crearTarjeta_OK() throws Exception {
        Tarjeta resp = new Tarjeta();
        resp.setId(1L);

        Mockito.when(tarjetaService.crearTarjeta(any(Tarjeta.class))).thenReturn(resp);

        String json = """
        {
          "numero": "1234567890123456",
          "titular": "Juan Perez",
          "saldo": 500,
          "estado": "activa",
          "fechaExpiracion": "2027-12-31"
        }
        """;

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
