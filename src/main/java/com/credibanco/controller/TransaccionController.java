package com.credibanco.controller;

import com.credibanco.dto.TransaccionRequest;
import com.credibanco.dto.TransaccionResponse;
import com.credibanco.mapper.ResponseMapper;
import com.credibanco.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final ResponseMapper responseMapper;

    public TransaccionController(TransaccionService transaccionService, ResponseMapper responseMapper) {
        this.transaccionService = transaccionService;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/compra")
    @Operation(summary = "Registra una compra")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public TransaccionResponse registrarCompra(@Valid @RequestBody TransaccionRequest request) {
        return responseMapper.toTransaccionResponse(
                transaccionService.registrarCompra(request.getTarjetaId(), request.getMonto()));
    }

    @PostMapping("/recarga")
    @Operation(summary = "Recarga saldo vía transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recarga registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public TransaccionResponse recargar(@Valid @RequestBody TransaccionRequest request) {
        return responseMapper.toTransaccionResponse(
                transaccionService.recargar(request.getTarjetaId(), request.getMonto()));
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anula una transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción anulada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    public TransaccionResponse anularTransaccion(@PathVariable Long id) {
        return responseMapper.toTransaccionResponse(transaccionService.anularTransaccion(id));
    }

    @GetMapping
    @Operation(summary = "Lista transacciones de una tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de transacciones")
    })
    public List<TransaccionResponse> listarTransaccionesPorTarjeta(@RequestParam Long tarjetaId) {
        return responseMapper.toTransaccionResponseList(transaccionService.listarTransaccionesPorTarjeta(tarjetaId));
    }
}
