package com.credibanco.controller;

import com.credibanco.models.Tarjeta;
import com.credibanco.service.TarjetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
@Tag(name = "Tarjetas", description = "Operaciones de tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService; }

    @PostMapping
    @Operation(summary = "Crea una tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Tarjeta crearTarjeta(@RequestBody Tarjeta tarjeta) {
        return tarjetaService.crearTarjeta(tarjeta); }


    @PutMapping("/{id}/recargar")
    @Operation(summary = "Recarga saldo a una tarjeta específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo recargado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")
    })
    public Tarjeta recargar(
            @Parameter(description = "ID de la tarjeta") @PathVariable Long id,
            @Parameter(description = "Monto a recargar") @RequestParam BigDecimal monto) {
        return tarjetaService.recargarSaldo(id, monto);
    }

    @GetMapping
    @Operation(summary = "Lista todas las tarjetas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de tarjetas")
    })
    public List<Tarjeta> listarTarjetas() {
        return tarjetaService.listarTarjetas();
    }
}
