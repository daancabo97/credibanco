package com.credibanco.security;

import com.credibanco.models.Usuario;
import com.credibanco.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevUsuarioInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public DevUsuarioInitializer(UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${app.security.bootstrap-user:}") String username,
                                 @Value("${app.security.bootstrap-password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario);
    }
}
