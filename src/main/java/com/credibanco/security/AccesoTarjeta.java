package com.credibanco.security;

import com.credibanco.exception.BusinessException;
import com.credibanco.models.Tarjeta;
import com.credibanco.models.Usuario;
import com.credibanco.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccesoTarjeta {

    private final UsuarioRepository usuarioRepository;

    public AccesoTarjeta(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return usuarioRepository.findByUsername(auth.getName()).orElse(null);
    }

    public void validar(Tarjeta tarjeta) {
        Usuario userActual = usuarioActual();
        if (userActual == null || tarjeta.getUsuario() == null) {
            return;
        }
        if (!userActual.getId().equals(tarjeta.getUsuario().getId())) {
            throw new BusinessException("Tarjeta no encontrada");
        }
    }
}
