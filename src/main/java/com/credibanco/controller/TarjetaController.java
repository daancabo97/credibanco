package com.credibanco.controller;

import com.credibanco.models.Tarjeta;
import com.credibanco.service.TarjetaService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {
    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) { this.tarjetaService = tarjetaService; }

    @PostMapping
    public Tarjeta crearTarjeta(@RequestBody Tarjeta tarjeta) { return tarjetaService.crearTarjeta(tarjeta); }

    @PutMapping("/{id}/recargar")
    public void recargarSaldo(@PathVariable Long id, @RequestParam BigDecimal monto) {
        tarjetaService.recargarSaldo(id, monto);
    }

    @GetMapping
    public List<Tarjeta> listarTarjetas() { return tarjetaService.listarTarjetas(); }
}
