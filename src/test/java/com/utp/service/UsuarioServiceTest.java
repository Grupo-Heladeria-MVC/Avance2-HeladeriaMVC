package com.utp.service;

import com.utp.model.Usuario;
import com.utp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {



        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UsuarioService usuarioService;

        private Usuario usuario;

        @BeforeEach
        void setUp() {
            usuario = new Usuario();
            usuario.setId(1);
            usuario.setUsername("user1");
            usuario.setEmail("user1@example.com");
            usuario.setPassword("encodedPass");
            usuario.setCreatedAt(LocalDateTime.now());
        }

        @Nested
        @DisplayName("findByUsername Method")
        class FindByUsernameTests {
            @Test
            @DisplayName("Should return user when found")
            void whenUserExists_thenReturnUsuario() {
                when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));

                Usuario result = usuarioService.findByUsername("user1");
                assertNotNull(result);
                assertEquals("user1", result.getUsername());
                verify(usuarioRepository).findByUsername("user1");
            }

            @Test
            @DisplayName("Should throw when user not found")
            void whenUserNotFound_thenThrowException() {
                when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());

                assertThrows(RuntimeException.class,
                        () -> usuarioService.findByUsername("not_exist"));
                verify(usuarioRepository).findByUsername("not_exist");
            }
        }

        @Nested
        @DisplayName("save Method")
        class SaveTests {
            @Test
            @DisplayName("Should save user when data is valid")
            void whenValidUsuario_thenSave() {
                when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());
                when(usuarioRepository.existsByEmail("user1@example.com")).thenReturn(false);
                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

                Usuario saved = usuarioService.save(usuario);
                assertNotNull(saved);
                assertEquals(1, saved.getId());
                verify(usuarioRepository).save(usuario);
            }

            @Test
            @DisplayName("Should throw when username already exists")
            void whenUsernameExists_thenThrow() {
                when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> usuarioService.save(usuario));
                assertEquals("El nombre de usuario ya existe", ex.getMessage());
            }

            @Test
            @DisplayName("Should throw when email already exists")
            void whenEmailExists_thenThrow() {
                when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());
                when(usuarioRepository.existsByEmail("user1@example.com")).thenReturn(true);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> usuarioService.save(usuario));
                assertEquals("El email ya está registrado", ex.getMessage());
            }

            @Test
            @DisplayName("Should throw when email format invalid")
            void whenInvalidEmailFormat_thenThrow() {
                usuario.setEmail("invalid-email");
                when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
                when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> usuarioService.save(usuario));
                assertEquals("Formato de email inválido", ex.getMessage());
            }
        }

        @Nested
        @DisplayName("verificarContraseñaActual Method")
        class PasswordVerificationTests {
            @Test
            @DisplayName("Should return true when passwords match")
            void whenPasswordsMatch_thenTrue() {
                when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
                boolean match = usuarioService.verificarContraseñaActual(usuario, "rawPass");
                assertTrue(match);
                verify(passwordEncoder).matches("rawPass", "encodedPass");
            }

            @Test
            @DisplayName("Should return false when passwords do not match")
            void whenPasswordsDoNotMatch_thenFalse() {
                when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(false);
                boolean match = usuarioService.verificarContraseñaActual(usuario, "rawPass");
                assertFalse(match);
            }
        }

        @Nested
        @DisplayName("findAllClientUsers Method")
        class FindAllClientUsersTests {
            @Test
            @DisplayName("Should return list of CLIENTE users")
            void whenInvoked_thenReturnList() {
                List<Usuario> list = Collections.singletonList(usuario);
                when(usuarioRepository.findByRoles_Nombre("CLIENTE")).thenReturn(list);

                List<Usuario> result = usuarioService.findAllClientUsers();
                assertEquals(1, result.size());
                verify(usuarioRepository).findByRoles_Nombre("CLIENTE");
            }

            @Test
            @DisplayName("Should return paged CLIENTE users")
            void whenPaged_thenReturnPage() {
                Page<Usuario> page = new PageImpl<>(Collections.singletonList(usuario));
                Pageable pageable = PageRequest.of(0, 10);
                when(usuarioRepository.findByRoles_Nombre("CLIENTE", pageable)).thenReturn(page);

                Page<Usuario> result = usuarioService.findAllClientUsers(pageable);
                assertEquals(1, result.getTotalElements());
                verify(usuarioRepository).findByRoles_Nombre("CLIENTE", pageable);
            }
        }


}
