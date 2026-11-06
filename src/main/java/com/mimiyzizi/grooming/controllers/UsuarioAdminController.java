package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.UsuarioAdminDTO;
import com.mimiyzizi.grooming.repository.UsuarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private static final int TAMANO_PAGINA = 10;

    private final UsuarioRepository usuarioRepository;

    public UsuarioAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listarUsuarios(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        String busqueda = q.trim();

        int paginaSolicitada = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                paginaSolicitada,
                TAMANO_PAGINA,
                Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        Page<Usuario> paginaUsuarios;

        if (busqueda.isBlank()) {

            paginaUsuarios = usuarioRepository.findAll(pageable);

        } else {

            paginaUsuarios = usuarioRepository
                    .findByNombreCompletoContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
                            busqueda,
                            busqueda,
                            busqueda,
                            pageable);
        }

        // Evitar permanecer en una página que ya no existe.
        if (paginaUsuarios.getTotalPages() > 0
                && paginaSolicitada >= paginaUsuarios.getTotalPages()) {

            paginaSolicitada = paginaUsuarios.getTotalPages() - 1;

            pageable = PageRequest.of(
                    paginaSolicitada,
                    TAMANO_PAGINA,
                    Sort.by(Sort.Direction.DESC, "fechaRegistro"));

            if (busqueda.isBlank()) {
                paginaUsuarios = usuarioRepository.findAll(pageable);
            } else {
                paginaUsuarios = usuarioRepository
                        .findByNombreCompletoContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
                                busqueda,
                                busqueda,
                                busqueda,
                                pageable);
            }
        }

        Usuario administrador = usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .orElse(null);

        String nombreAdministrador = administrador != null
                ? administrador.getNombreCompleto()
                : "Administrador";

        model.addAttribute(
                "usuarios",
                paginaUsuarios.getContent()
                        .stream()
                        .map(UsuarioAdminDTO::desde)
                        .toList());

        model.addAttribute("nombreAdministrador", nombreAdministrador);
        model.addAttribute("paginaAdmin", "usuarios");
        model.addAttribute("busqueda", busqueda);

        model.addAttribute("paginaActual", paginaUsuarios.getNumber());
        model.addAttribute("totalPaginas", paginaUsuarios.getTotalPages());
        model.addAttribute("totalUsuarios", paginaUsuarios.getTotalElements());

        model.addAttribute("hayAnterior", paginaUsuarios.hasPrevious());
        model.addAttribute("haySiguiente", paginaUsuarios.hasNext());

        long desde = paginaUsuarios.getTotalElements() == 0
                ? 0
                : (long) paginaUsuarios.getNumber() * TAMANO_PAGINA + 1;

        long hasta = desde == 0
                ? 0
                : desde + paginaUsuarios.getNumberOfElements() - 1;

        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        return "admin/usuarios";
    }
}