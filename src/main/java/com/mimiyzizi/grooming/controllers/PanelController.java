package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    private final UsuarioRepository usuarioRepository;

    public PanelController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/cliente/inicio")
    public String inicioCliente(Authentication authentication, Model model) {
        usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "cliente/inicio";
    }

    @GetMapping("/admin/inicio")
    public String inicioAdmin(Authentication authentication, Model model) {
        model.addAttribute("paginaAdmin", "inicio");
        String nombreAdministrador = usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .map(usuario -> usuario.getNombreCompleto())
                .orElse("Administrador");

        model.addAttribute("nombreAdministrador", nombreAdministrador);

        return "admin/inicio";
    }

}
