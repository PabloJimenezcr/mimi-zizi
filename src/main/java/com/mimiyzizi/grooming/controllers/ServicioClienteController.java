package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.service.ServicioService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente/servicios")
public class ServicioClienteController {

    private final ServicioService servicioService;

    public ServicioClienteController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public String catalogoServicios(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "servicios",
                servicioService.listarServiciosActivos());

        model.addAttribute("paginaCliente", "servicios");

        return "cliente/servicios/catalogo";
    }
}