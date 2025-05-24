package com.utp.heladeriaBreezemvc.controller;

import com.utp.heladeriaBreezemvc.model.Producto;
import com.utp.heladeriaBreezemvc.model.Usuario;
import com.utp.heladeriaBreezemvc.security.SecurityUserDetails;
import com.utp.heladeriaBreezemvc.service.ProductoService;
import com.utp.heladeriaBreezemvc.service.UsuarioService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
     @Autowired
    private ProductoService productoService;
      @Autowired
    private UsuarioService usuarioService;
     
    @GetMapping("/home")
    public String homeCliente(Authentication authentication, Model model) {
        
    
        List<Producto> productosDisponibles = productoService.findAllAvailable();
        
        // Obtener 6 productos aleatorios de los disponibles
        List<Producto> productosAleatorios = productosDisponibles.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collected -> {
                            Collections.shuffle(collected);
                            return collected.stream()
                                    .limit(6)
                                    .collect(Collectors.toList());
                        }));
                        
        model.addAttribute("productos", productosAleatorios);
    
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return "redirect:/login?error";
        }
        
        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        Usuario usuarioActual = userDetails.getUsuario();
        model.addAttribute("usuario", usuarioActual);
        return "cliente/homeCliente";

    }
    
}
