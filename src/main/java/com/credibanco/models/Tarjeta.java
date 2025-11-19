package com.credibanco.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "tipo_tarjeta", length = 10, nullable = false)
    private String tipoTarjeta;

    @Column(name = "saldo", nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @OneToMany(mappedBy = "tarjeta")
    @JsonManagedReference
    private List<Transaccion> transacciones;
}