package com.utp;

import com.utp.service.*;
import com.utp.model.*;
import com.utp.repository.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;


// ============================================================================
// TEST PARA USUARIO SERVICE - RESOLUCIÓN INCI-19
/**
 * Test unitarios para resolver INCI-17: Exposición de información sensible en validaciones
 * 
 * PROBLEMA IDENTIFICADO:
 * El método actualizarDetalle() en UsuarioController expone información sensible
 * al mostrar diferentes mensajes de error específicos que permiten a un atacante:
 * - Determinar si una contraseña actual es correcta
 * - Identificar emails y usernames ya registrados
 * - Realizar ataques de enumeración de usuarios
 * 
 * SOLUCIÓN IMPLEMENTADA:
 * - Crear método actualizarUsuarioSeguro() que unifique mensajes de error
 * - Implementar logging de seguridad para detectar intentos maliciosos
 * - Validar credenciales sin exponer información específica
 */
// ============================================================================


public class UsuarioUpdateSecurityTest {
    
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
    @DisplayName("Debe actualizar usuario exitosamente con datos válidos")
    void testActualizarUsuarioSeguro_DatosValidos_DebeRetornarExito() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("testuser");
        usuario.setEmail("test@email.com");
        usuario.setPassword("passwordEncriptada");
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("nuevo@email.com")).thenReturn(false);
        when(usuarioRepository.existsByUsername("nuevouser")).thenReturn(false);
        when(passwordEncoder.matches("passwordActual", "passwordEncriptada")).thenReturn(true);
        when(passwordEncoder.encode("nuevaPassword")).thenReturn("nuevaPasswordEncriptada");
        
        // Act
        RegistroResult resultado = usuarioService.actualizarUsuarioSeguro(
            1, "nuevouser", "nuevo@email.com", "passwordActual", "nuevaPassword", "192.168.1.1"
        );
        
        // Assert
        assertTrue(resultado.isExito());
        assertEquals("Datos actualizados correctamente", resultado.getMensaje());
        verify(securityLoggingService).logUpdateAttempt(1, "192.168.1.1", true);
    }
    
    @Test
    @DisplayName("No debe revelar información cuando contraseña actual es incorrecta")
    void testActualizarUsuarioSeguro_PasswordIncorrecta_NoDebeRevelarInfo() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setPassword("passwordEncriptada");
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("passwordIncorrecta", "passwordEncriptada")).thenReturn(false);
        
        // Act
        RegistroResult resultado = usuarioService.actualizarUsuarioSeguro(
            1, "nuevouser", "nuevo@email.com", "passwordIncorrecta", "nuevaPassword", "192.168.1.1"
        );
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Error al actualizar los datos", resultado.getMensaje()); // Mensaje genérico
        verify(securityLoggingService).logUpdateAttempt(1, "192.168.1.1", false);
    }
    
    @Test
    @DisplayName("No debe revelar información cuando email ya existe")
    void testActualizarUsuarioSeguro_EmailExistente_NoDebeRevelarInfo() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setPassword("passwordEncriptada");
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("existente@email.com")).thenReturn(true);
        when(passwordEncoder.matches("passwordActual", "passwordEncriptada")).thenReturn(true);
        
        // Act
        RegistroResult resultado = usuarioService.actualizarUsuarioSeguro(
            1, "nuevouser", "existente@email.com", "passwordActual", null, "192.168.1.1"
        );
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Error al actualizar los datos", resultado.getMensaje()); // Mensaje genérico
        verify(securityLoggingService).logUpdateAttempt(1, "192.168.1.1", false);
    }
    
    @Test
    @DisplayName("No debe revelar información cuando username ya existe")
    void testActualizarUsuarioSeguro_UsernameExistente_NoDebeRevelarInfo() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setPassword("passwordEncriptada");
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsername("userexistente")).thenReturn(true);
        when(passwordEncoder.matches("passwordActual", "passwordEncriptada")).thenReturn(true);
        
        // Act
        RegistroResult resultado = usuarioService.actualizarUsuarioSeguro(
            1, "userexistente", "nuevo@email.com", "passwordActual", null, "192.168.1.1"
        );
        
        // Assert
        assertFalse(resultado.isExito());
        assertEquals("Error al actualizar los datos", resultado.getMensaje()); // Mensaje genérico
        verify(securityLoggingService).logUpdateAttempt(1, "192.168.1.1", false);
    }
    
    @Test
    @DisplayName("Debe verificar contraseña actual correctamente")
    void testVerificarContraseñaActual_PasswordCorrecta_DebeRetornarTrue() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setPassword("passwordEncriptada");
        String passwordActual = "miPassword";
        
        when(passwordEncoder.matches(passwordActual, "passwordEncriptada")).thenReturn(true);
        
        // Act
        boolean resultado = usuarioService.verificarContraseñaActual(usuario, passwordActual);
        
        // Assert
        assertTrue(resultado);
        verify(passwordEncoder).matches(passwordActual, "passwordEncriptada");
    }
    
    @Test
    @DisplayName("Debe verificar contraseña actual incorrecta")
    void testVerificarContraseñaActual_PasswordIncorrecta_DebeRetornarFalse() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setPassword("passwordEncriptada");
        String passwordIncorrecta = "passwordMala";
        
        when(passwordEncoder.matches(passwordIncorrecta, "passwordEncriptada")).thenReturn(false);
        
        // Act
        boolean resultado = usuarioService.verificarContraseñaActual(usuario, passwordIncorrecta);
        
        // Assert
        assertFalse(resultado);
        verify(passwordEncoder).matches(passwordIncorrecta, "passwordEncriptada");
    }
 
}
