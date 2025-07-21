package com.utp.service;

import com.utp.model.*;
import com.utp.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Usuario1Service {
    
     @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SecurityLoggingService securityLoggingService;
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    
    // Método para obtener un usuario por ID
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    // MÉTODO SEGURO PARA PROCESAR REGISTRO
    public RegistroResult procesarRegistroSeguro(String nombre, String email, String password, String ipAddress) {
        try {
            // Simular tiempo de procesamiento consistente
            Thread.sleep(200 + (long)(Math.random() * 100));
            
            // Validaciones básicas
            if (!esEmailValido(email)) {
                securityLoggingService.logRegistrationAttempt(email, ipAddress, false);
                return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
            }
            
            if (!esPasswordValida(password)) {
                securityLoggingService.logRegistrationAttempt(email, ipAddress, false);
                return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
            }
            
            if (!esNombreValido(nombre)) {
                securityLoggingService.logRegistrationAttempt(email, ipAddress, false);
                return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
            }
            
            // Verificar si el email ya existe (sin exponer esta información)
            boolean emailExiste = usuarioRepository.existsByEmail(email);
            
            if (emailExiste) {
                // NO revelar que el email ya existe
                securityLoggingService.logRegistrationAttempt(email, ipAddress, false);
                return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
            }
            
            // Si todo está bien, proceder con el registro
            securityLoggingService.logRegistrationAttempt(email, ipAddress, true);
            return new RegistroResult(true, "Se ha enviado un código de verificación a su email");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
        } catch (Exception e) {
            logger.error("Error en procesarRegistroSeguro: ", e);
            securityLoggingService.logRegistrationAttempt(email, ipAddress, false);
            return new RegistroResult(false, "Se ha enviado un código de verificación a su email");
        }
    }
    
    public Usuario save(Usuario usuario) {
        // Validaciones existentes
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        return usuarioRepository.save(usuario);
    }
    
    // Métodos de validación
    private boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private boolean esPasswordValida(String password) {
        return password != null && password.length() >= 8; // Cambié de 6 a 8 para mayor seguridad
    }
    
    private boolean esNombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty() && nombre.length() >= 2;
    }
    
    public List<Usuario> findAllClientUsers() {
        return usuarioRepository.findByRoles_Nombre("CLIENTE");
    }
    
    public Page<Usuario> findAllClientUsers(Pageable pageable) {
        return usuarioRepository.findByRoles_Nombre("CLIENTE", pageable);
    }
    
    public boolean verificarContraseñaActual(Usuario usuario, String contraseñaActual) {
        return passwordEncoder.matches(contraseñaActual, usuario.getPassword());
    }
    
     // NUEVO MÉTODO SEGURO
    public RegistroResult actualizarUsuarioSeguro(Integer userId, String nuevoUsername, 
            String nuevoEmail, String passwordActual, String nuevaPassword, String ip) {
        
        try {
            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
            if (usuarioOpt.isEmpty()) {
                securityLoggingService.logUpdateAttempt(userId, ip, false);
                return new RegistroResult(false, "Error al actualizar los datos");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Si se quiere cambiar contraseña, verificar la actual
            if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
                if (passwordActual == null || passwordActual.isEmpty()) {
                    securityLoggingService.logUpdateAttempt(userId, ip, false);
                    return new RegistroResult(false, "Error al actualizar los datos");
                }
                
                if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                    securityLoggingService.logFailedPasswordAttempt(usuario.getUsername(), ip);
                    return new RegistroResult(false, "Error al actualizar los datos");
                }
                
                // Validar nueva contraseña
                if (!esPasswordValida(nuevaPassword)) {
                    securityLoggingService.logUpdateAttempt(userId, ip, false);
                    return new RegistroResult(false, "Error al actualizar los datos");
                }
                
                usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            }
            
            // Validar email si cambió
            if (nuevoEmail != null && !nuevoEmail.equals(usuario.getEmail())) {
                if (!esEmailValido(nuevoEmail) || usuarioRepository.existsByEmail(nuevoEmail)) {
                    securityLoggingService.logUpdateAttempt(userId, ip, false);
                    return new RegistroResult(false, "Error al actualizar los datos");
                }
                usuario.setEmail(nuevoEmail);
            }
            
            // Validar username si cambió
            if (nuevoUsername != null && !nuevoUsername.equals(usuario.getUsername())) {
                if (usuarioRepository.existsByUsername(nuevoUsername)) {
                    securityLoggingService.logUpdateAttempt(userId, ip, false);
                    return new RegistroResult(false, "Error al actualizar los datos");
                }
                usuario.setUsername(nuevoUsername);
            }
            
            usuarioRepository.save(usuario);
            securityLoggingService.logUpdateAttempt(userId, ip, true);
            return new RegistroResult(true, "Datos actualizados correctamente");
            
        } catch (Exception e) {
            securityLoggingService.logUpdateAttempt(userId, ip, false);
            return new RegistroResult(false, "Error al actualizar los datos");
        }
    }
    
    // MÉTODO HELPER PARA OBTENER IP
    public String obtenerIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    
}
