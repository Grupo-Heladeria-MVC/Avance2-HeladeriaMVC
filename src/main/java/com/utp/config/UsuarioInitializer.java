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
        
        Rol rolCliente = rolRepository.findByNombre("CLIENTE");
        if (rolCliente == null) {
            rolCliente = new Rol("CLIENTE");
            rolRepository.save(rolCliente);
        }
        
        // Crear usuario administrador si no existe (verificar por username Y email)
        if (!usuarioRepository.existsByUsername("admin") && !usuarioRepository.existsByEmail("admin@example.com")) {
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
        
        // Crear usuario open si no existe (verificar por username Y email)
        if (!usuarioRepository.existsByUsername("open12") && !usuarioRepository.existsByEmail("open@gmail.com")) {
            Usuario open = new Usuario();
            open.setUsername("open12");
            open.setPassword(passwordEncoder.encode("open12345"));
            open.setEmail("open@gmail.com");
            open.setNombre("Open Plaza");
            open.setEnabled(true);
            open.setRoles(new HashSet<>());
            open.getRoles().add(rolOpen);
            usuarioRepository.save(open);
        }
                   
        // Crear usuario udep si no existe (verificar por username Y email)
        if (!usuarioRepository.existsByUsername("udep11") && !usuarioRepository.existsByEmail("udep@gmail.com")) {
            Usuario udep = new Usuario();
            udep.setUsername("udep11");
            udep.setPassword(passwordEncoder.encode("udep12345"));
            udep.setEmail("udep@gmail.com");
            udep.setNombre("Universidad de Piura");
            udep.setEnabled(true);
            udep.setRoles(new HashSet<>());
            udep.getRoles().add(rolUdep);
            usuarioRepository.save(udep);
        }
    }
}