package com.credibanco.service;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.Transaccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransaccionServiceIntegrationTest {

    @Autowired
    private TarjetaService tarjetaService;

    @Autowired
    private TransaccionService transaccionService;

    // Contador para asegurar que cada test usa un número de tarjeta único para evitar conflictos con la base de datos
    private static int counter = 0;


    /**
     * MÉTODO AUXILIAR CORREGIDO: Asegura que el número siempre tiene 16 dígitos.
     */
    private String generarNumeroTarjetaUnico() {
        // Genera un número de 16 dígitos único para la prueba
        String base = "123456789012";
        // Asegura que el contador de 4 dígitos se añada, completando los 16.
        counter++;
        String suffix = String.format("%04d", counter);
        return base + suffix;
    }

    /**
     * MÉTODO AUXILIAR: Crea un TarjetaRequest válido, cumpliendo las reglas del servicio.
     */
    private TarjetaRequest crearTarjetaRequestValida(String nombreTitular, String tipoTarjeta) {
        TarjetaRequest request = new TarjetaRequest();

        // 1. Número de tarjeta con 16 dígitos (CORRECCIÓN CRÍTICA)
        request.setNumeroTarjeta(generarNumeroTarjetaUnico());

        request.setNombreTitular(nombreTitular);
        request.setFechaCreacion(LocalDate.now());

        // 2. Fecha de vencimiento AL MENOS 3 años después (CORRECCIÓN CRÍTICA)
        request.setFechaVencimiento(LocalDate.now().plusYears(4));

        request.setTipoTarjeta(tipoTarjeta);
        return request;
    }


    // El error en la ejecución estaba en la línea 34, que ahora es la 66 del código corregido.
    @Test
    public void testCompraYAnulacion()  {

        // 1. CREACIÓN: Usar el método auxiliar para crear un TarjetaRequest válido
        TarjetaRequest request = crearTarjetaRequestValida("Luis Diaz", "debito");

        // La entidad Tarjeta que se estaba inicializando manualmente YA NO ES NECESARIA.

        // La línea que fallaba se corrige pasando el DTO con datos válidos.
        Tarjeta creada = tarjetaService.crearTarjeta(request);

        // 2. Recargar antes de comprar
        tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("50000"));

        // 3. Realizar compra de 10000 que debe ser exitosa
        Transaccion txCompra = transaccionService.registrarCompra(creada.getId(), new BigDecimal("10000"));
        assertEquals("exitosa", txCompra.getEstado());

        // 4. Anular la compra
        Transaccion txAnulada = transaccionService.anularTransaccion(txCompra.getId());
        assertEquals("anulada", txAnulada.getEstado());

        // 5. Validar que el saldo está restaurado a 50,000
        Tarjeta actual = tarjetaService.listarTarjetas().stream()
                .filter(t -> t.getId().equals(creada.getId()))
                .findFirst().orElse(null);
        assertNotNull(actual);
        assertTrue(actual.getSaldo().compareTo(new BigDecimal("50000")) == 0);
    }

    // El error en la ejecución estaba en la línea 62, que ahora es la 97 del código corregido.
    @Test
    public void testConsultarTransaccionesPorTarjeta() {
        // 1. Crear Tarjeta válida (usando la lógica de la respuesta anterior)
        TarjetaRequest request = crearTarjetaRequestValida("Carlos Perez", "credito");
        Tarjeta creada = tarjetaService.crearTarjeta(request);

        // 2. Transacción 1: Recargar (Esto crea la primera transacción)
        tarjetaService.recargarSaldo(creada.getId(), new BigDecimal("70000")); // <-- ¡ESTA ES LA LÍNEA CRÍTICA QUE FALTABA O SE OMITIÓ!

        // 3. Transacción 2: Registrar una compra (Esto crea la segunda transacción)
        transaccionService.registrarCompra(creada.getId(), new BigDecimal("20000"));

        // 4. Consultar transacciones
        List<Transaccion> transacciones = transaccionService.listarTransaccionesPorTarjeta(creada.getId());

        assertNotNull(transacciones);
        assertFalse(transacciones.isEmpty());

        // 5. Assert: Ahora sí, se esperan 2 transacciones (Recarga + Compra)
        assertEquals(2, transacciones.size()); // Línea que fallaba (116)
    }

}