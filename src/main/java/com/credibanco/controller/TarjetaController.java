package com.credibanco.controller;

import com.credibanco.dto.TarjetaRequest;
import com.credibanco.dto.TarjetaResponse;
import com.credibanco.mapper.ResponseMapper;
import com.credibanco.service.TarjetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
@Tag(name = "Tarjetas", description = "Operaciones de tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;
    private final ResponseMapper responseMapper;

    public TarjetaController(TarjetaService tarjetaService, ResponseMapper responseMapper) {
        this.tarjetaService = tarjetaService;
        this.responseMapper = responseMapper;
    }

    @PostMapping
    @Operation(summary = "Crea una tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public TarjetaResponse crearTarjeta(@Valid @RequestBody TarjetaRequest tarjetaRequest) {
        return responseMapper.toTarjetaCreadaResponse(tarjetaService.crearTarjeta(tarjetaRequest));
    }


    @PutMapping("/{id}/recargar")
    @Operation(summary = "Recarga saldo a una tarjeta específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo recargado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")
    })
    public TarjetaResponse recargar(
            @Parameter(description = "ID de la tarjeta") @PathVariable Long id,
            @Parameter(description = "Monto a recargar") @RequestParam BigDecimal monto) {
        return responseMapper.toTarjetaEnmascarada(tarjetaService.recargarSaldo(id, monto));
    }

    @GetMapping
    @Operation(summary = "Lista las tarjetas del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de tarjetas")
    })
    public List<TarjetaResponse> listarTarjetas() {
        return responseMapper.toTarjetaEnmascarada(tarjetaService.listarTarjetas());
    }
}
