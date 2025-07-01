package com.utp;
import com.utp.model.*;
import com.utp.repository.*;
import com.utp.service.*;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


// ============================================================================
// TEST PARA USUARIO SERVICE 
/**
 * Test unitarios para resolver INCI-1: Vulnerabilidad de enumeración de usuarios
 * 
 * PROBLEMA IDENTIFICADO:
 * El método iniciarRegistro() en RegistroController expone información sensible
 * al mostrar diferentes mensajes de error que permiten a un atacante determinar
 * si un email está registrado o no.
 * 
 * SOLUCIÓN IMPLEMENTADA:
 * - MODIFICAR el RegistroController existente para que sea seguro
 * - Unificar mensajes de respuesta independientemente del estado del email
 * - Implementar rate limiting para prevenir ataques de fuerza bruta
 * - Agregar logging de seguridad para detectar intentos de enumeración
 */
// ============================================================================

public class UsuarioServiceSecurityTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private SecurityLoggingService securityLoggingService;
    
    @InjectMocks
    private Usuario1Service usuarioService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("Debe procesar registro exitoso con email válido")
    void testProcesarRegistroSeguro_EmailValido_DebeRetornarExito() {
        // Arrange
        String nombre = "Juan Perez";
        String email = "juan@test.com";
        String password = "123456";
        String ip = "192.168.1.1";
        
        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        
        // Act
        RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ip);
        
        // Assert
        assertTrue(resultado.isExito());
        assertEquals("Se ha enviado un código de verificación a su email", resultado.getMensaje());
        verify(securityLoggingService).logRegistrationAttempt(email, ip, true);
    }
    
    @Test
    @DisplayName("No debe revelar información cuando email ya existe")
    void testProcesarRegistroSeguro_EmailExistente_NoDebeRevelarInformacion() {
        // Arrange
        String nombre = "Juan";
        String email = "existente@test.com";
        String password = "123456";
        String ip = "192.168.1.1";
        
        when(usuarioRepository.existsByEmail(email)).thenReturn(true);
        
        // Act
        RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ip);
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Se ha enviado un código de verificación a su email", resultado.getMensaje());
        verify(securityLoggingService).logRegistrationAttempt(email, ip, false);
    }
    
    @Test
    @DisplayName("Debe manejar email inválido con mensaje consistente")
    void testProcesarRegistroSeguro_EmailInvalido_DebeMantenerConsistencia() {
        // Arrange
        String nombre = "Juan";
        String email = "email-invalido";
        String password = "123456";
        String ip = "192.168.1.1";
        
        // Act
        RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ip);
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Se ha enviado un código de verificación a su email", resultado.getMensaje());
        verify(securityLoggingService).logRegistrationAttempt(email, ip, false);
    }
    
    @Test
    @DisplayName("Debe manejar contraseña débil con mensaje consistente")
    void testProcesarRegistroSeguro_PasswordDebil_DebeMantenerConsistencia() {
        // Arrange
        String nombre = "Juan";
        String email = "juan@test.com";
        String password = "123"; // Muy corta
        String ip = "192.168.1.1";
        
        // Act
        RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ip);
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Se ha enviado un código de verificación a su email", resultado.getMensaje());
        verify(securityLoggingService).logRegistrationAttempt(email, ip, false);
    }
    
    @Test
    @DisplayName("Debe manejar nombre inválido con mensaje consistente")
    void testProcesarRegistroSeguro_NombreInvalido_DebeMantenerConsistencia() {
        // Arrange
        String nombre = ""; // Vacío
        String email = "juan@test.com";
        String password = "123456";
        String ip = "192.168.1.1";
        
        // Act
        RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ip);
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Se ha enviado un código de verificación a su email", resultado.getMensaje());
        verify(securityLoggingService).logRegistrationAttempt(email, ip, false);
    }
    
    @Test
    @DisplayName("Debe encontrar usuario por username")
    void testFindByUsername_UsuarioExiste_DebeRetornarUsuario() {
        // Arrange
        String username = "testuser";
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));
        
        // Act
        Usuario resultado = usuarioService.findByUsername(username);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(username, resultado.getUsername());
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void testFindByUsername_UsuarioNoExiste_DebeLanzarExcepcion() {
        // Arrange
        String username = "noexiste";
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.findByUsername(username);
        });
    }
    
    @Test
    @DisplayName("Debe verificar si email existe")
    void testExistsByEmail_EmailExiste_DebeRetornarTrue() {
        // Arrange
        String email = "test@example.com";
        when(usuarioRepository.existsByEmail(email)).thenReturn(true);
        
        // Act
        boolean resultado = usuarioService.existsByEmail(email);
        
        // Assert
        assertTrue(resultado);
    }
}
