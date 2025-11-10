package com.credibanco.controller;

import com.credibanco.models.Transaccion;
import com.credibanco.service.TransaccionService;
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
    public Transaccion registrarCompra(@RequestParam Long tarjetaId, @RequestParam BigDecimal monto) {
        return transaccionService.registrarCompra(tarjetaId, monto);
    }

    @PostMapping("/recarga")
    public Transaccion recargar(@RequestParam Long tarjetaId, @RequestParam BigDecimal monto) {
        return transaccionService.recargar(tarjetaId, monto);
    }

    @PutMapping("/{id}/anular")
    public Transaccion anularTransaccion(@PathVariable Long id) {
        return transaccionService.anularTransaccion(id);
    }

    @GetMapping
    public List<Transaccion> listarTransaccionesPorTarjeta(@RequestParam Long tarjetaId) {
        return transaccionService.listarTransaccionesPorTarjeta(tarjetaId);
    }
}
