package com.utp.heladeriaBreezemvc.controller;

import com.utp.heladeriaBreezemvc.service.*;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Muestra la página de login
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "administrador/login";
    }
    
    // Maneja la redirección después del login exitoso
    @GetMapping("/redirect")
    public String redirectBasedOnRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Verificar si el usuario tiene rol de ADMIN
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/home";
        }
        // Verificar si el usuario tiene rol de CLIENTE
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return "redirect:/cliente/home";
        }
        
         else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPEN"))) {
            return "redirect:/open/home";
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_UDEP"))) {
            return "redirect:/udep/home";
        }
        
        return "redirect:/login?error";
    }
    
    
}