package com.utp.config;

import com.utp.model.*;
import com.utp.repository.*;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @PostConstruct
    public void init() {

        // Crear roles si no existen
        Rol rolAdmin = rolRepository.findByNombre("ADMIN");
        if (rolAdmin == null) {
            rolAdmin = new Rol("ADMIN");
            rolRepository.save(rolAdmin);
        }

        Rol rolOpen = rolRepository.findByNombre("OPEN");
        if (rolOpen == null) {
            rolOpen = new Rol("OPEN");
            rolRepository.save(rolOpen);
        }

        Rol rolUdep = rolRepository.findByNombre("UDEP");
        if (rolUdep == null) {
            rolUdep = new Rol("UDEP");
            rolRepository.save(rolUdep);
        }

        // Añadir rol CLIENTE
        Rol rolCliente = rolRepository.findByNombre("CLIENTE");
        if (rolCliente == null) {
            rolCliente = new Rol("CLIENTE");
            rolRepository.save(rolCliente);
        }

        // Crear usuario administrador si no existe
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin12345"));
            admin.setEmail("admin@example.com");
            admin.setNombre("Administrador");
            admin.setEnabled(true);
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(rolAdmin);

            usuarioRepository.save(admin);
        }

    }

}
