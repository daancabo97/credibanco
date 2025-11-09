package com.credibanco.repository;

import com.credibanco.models.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByTarjetaId(Long tarjetaId);
    List<Transaccion> findByEstado(String estado);
}

