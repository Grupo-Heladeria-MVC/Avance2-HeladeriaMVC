package com.utp.controller;

import com.utp.model.Usuario;
import com.utp.service.RegistroResult;
import com.utp.service.Usuario1Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    private Usuario1Service usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Datos del usuario HU-106
    @GetMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public String mostrarPerfil(Model model, Authentication authentication) {
        // Obtener el username del usuario autenticado
        String username = authentication.getName();

        // Obtener los datos del usuario
        Usuario usuario = usuarioService.findByUsername(username);

        // Añadir el usuario al modelo
        model.addAttribute("usuario", usuario);

        return "cliente/perfil";
    }

    @PostMapping("/perfil/actualizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String actualizarPerfil(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            // Obtener el usuario actual
            String username = authentication.getName();
            Usuario usuarioActual = usuarioService.findByUsername(username);

            // Actualizar solo los campos permitidos
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setEmail(usuario.getEmail());
            usuarioActual.setDireccion(usuario.getDireccion());
            usuarioActual.setTelefono(usuario.getTelefono());
            usuarioActual.setUsername(usuario.getUsername());

            usuarioService.save(usuarioActual);
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }

        return "redirect:/cliente/perfil";
    }

    @GetMapping("/detalle")
    @PreAuthorize("hasRole('CLIENTE')")
    public String mostrarDetalle(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);
        model.addAttribute("usuario", usuario);
        return "cliente/detalle";
    }

    //Agregar Validaciones de datos para actualizar informacion personal del cliente
    // MÉTODO MODIFICADO PARA SER SEGURO
    @PostMapping("/detalle/actualizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String actualizarDetalleSeguro(
            @ModelAttribute Usuario usuario,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            HttpServletRequest request) {
        
        try {
            String username = authentication.getName();
            Usuario usuarioActual = usuarioService.findByUsername(username);
            String ip = usuarioService.obtenerIpCliente(request);
            
            // Validación de contraseñas nuevas (esto SÍ podemos mostrar al usuario)
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                    return "redirect:/cliente/detalle";
                }
                
                if (newPassword.length() < 8) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                    return "redirect:/cliente/detalle";
                }
            }
            
            // Usar el método seguro para actualizar
            RegistroResult resultado = usuarioService.actualizarUsuarioSeguro(
                usuarioActual.getId(), 
                usuario.getUsername(), 
                usuario.getEmail(), 
                currentPassword, 
                newPassword, 
                ip
            );
            
            // Solo mostrar mensajes genéricos
            if (resultado.isExito()) {
                redirectAttributes.addFlashAttribute("mensaje", resultado.getMensaje());
            } else {
                redirectAttributes.addFlashAttribute("error", resultado.getMensaje()); // Siempre genérico
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar los datos");
        }
        
        return "redirect:/cliente/detalle";
    }

}
