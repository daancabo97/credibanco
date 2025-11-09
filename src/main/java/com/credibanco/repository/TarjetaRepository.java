package com.credibanco.repository;

import com.credibanco.models.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    Optional<Tarjeta> findByNumeroTarjeta(String numeroTarjeta);
}
