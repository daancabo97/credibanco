package com.credibanco.models;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarjeta")
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_tarjeta", length = 16, unique = true, nullable = false)
    private String numeroTarjeta;

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_vencimiento", length = 6, nullable = false)
    private String fechaVencimiento;

    @Column(name = "tipo_tarjeta", length = 10, nullable = false)
    private String tipoTarjeta;

    @Column(name = "saldo", nullable = false)
    private BigDecimal saldo;

    @OneToMany(mappedBy = "tarjeta")
    private List<Transaccion> transacciones;

    // Getters y setters
}