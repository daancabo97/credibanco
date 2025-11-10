package com.credibanco.controller;

import com.credibanco.models.Transaccion;
import com.credibanco.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/compra")
    @Operation(summary = "Registra una compra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Transaccion registrarCompra(@RequestParam Long tarjetaId, @RequestParam BigDecimal monto) {
        return transaccionService.registrarCompra(tarjetaId, monto);
    }

    @PostMapping("/recarga")
    @Operation(summary = "Recarga saldo vía transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recarga registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Transaccion recargar(@RequestParam Long tarjetaId, @RequestParam BigDecimal monto) {
        return transaccionService.recargar(tarjetaId, monto);
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anula una transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción anulada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    public Transaccion anularTransaccion(@PathVariable Long id) {
        return transaccionService.anularTransaccion(id);
    }

    @GetMapping
    @Operation(summary = "Lista transacciones de una tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de transacciones")
    })
    public List<Transaccion> listarTransaccionesPorTarjeta(@RequestParam Long tarjetaId) {
        return transaccionService.listarTransaccionesPorTarjeta(tarjetaId);
    }
}
