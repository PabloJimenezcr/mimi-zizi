package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.ServicioDTO;
import com.mimiyzizi.grooming.dto.ServicioForm;
import com.mimiyzizi.grooming.repository.UsuarioRepository;
import com.mimiyzizi.grooming.service.ServicioService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/servicios")
public class ServicioAdminController {

    private static final int TAMANO_PAGINA = 10;

    private final ServicioService servicioService;
    private final UsuarioRepository usuarioRepository;

    public ServicioAdminController(
            ServicioService servicioService,
            UsuarioRepository usuarioRepository) {

        this.servicioService = servicioService;
        this.usuarioRepository = usuarioRepository;
    }

    // LISTAR Y BUSCAR SERVICIOS
    @GetMapping
    public String listarServicios(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "todos") String estado,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        String busqueda = q.trim();

        String filtroEstado = switch (estado) {
            case "activos", "inactivos" -> estado;
            default -> "todos";
        };

        Boolean activo = switch (filtroEstado) {
            case "activos" -> true;
            case "inactivos" -> false;
            default -> null;
        };

        int paginaSolicitada = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                paginaSolicitada,
                TAMANO_PAGINA,
                Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        Page<ServicioDTO> paginaServicios = servicioService.buscarAdministracion(
                busqueda,
                activo,
                pageable);

        // Evitar páginas inexistentes al cambiar los filtros.
        if (paginaServicios.getTotalPages() > 0
                && paginaSolicitada >= paginaServicios.getTotalPages()) {

            paginaSolicitada = paginaServicios.getTotalPages() - 1;

            pageable = PageRequest.of(
                    paginaSolicitada,
                    TAMANO_PAGINA,
                    Sort.by(Sort.Direction.DESC, "fechaRegistro"));

            paginaServicios = servicioService.buscarAdministracion(
                    busqueda,
                    activo,
                    pageable);
        }

        model.addAttribute("servicios", paginaServicios.getContent());

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", filtroEstado);

        model.addAttribute("paginaActual", paginaServicios.getNumber());
        model.addAttribute("totalPaginas", paginaServicios.getTotalPages());

        model.addAttribute("hayAnterior", paginaServicios.hasPrevious());
        model.addAttribute("haySiguiente", paginaServicios.hasNext());

        model.addAttribute("totalServicios", servicioService.contarServicios());
        model.addAttribute("serviciosActivos", servicioService.contarServiciosActivos());
        model.addAttribute("serviciosInactivos", servicioService.contarServiciosInactivos());

        long desde = paginaServicios.getTotalElements() == 0
                ? 0
                : (long) paginaServicios.getNumber() * TAMANO_PAGINA + 1;

        long hasta = desde == 0
                ? 0
                : desde + paginaServicios.getNumberOfElements() - 1;

        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        model.addAttribute("paginaAdmin", "servicios");
        agregarAdministrador(model, authentication);

        return "admin/servicios/listado";
    }

    // MOSTRAR FORMULARIO DE CREACIÓN
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(
            Authentication authentication,
            Model model) {

        model.addAttribute("servicioForm", new ServicioForm());
        model.addAttribute("modoEdicion", false);
        model.addAttribute("paginaAdmin", "servicios");

        agregarAdministrador(model, authentication);

        return "admin/servicios/formulario";
    }

    // CREAR SERVICIO
    @PostMapping
    public String crearServicio(
            @Valid @ModelAttribute("servicioForm") ServicioForm formulario,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            prepararFormulario(model, authentication, false, null);

            return "admin/servicios/formulario";
        }

        try {

            servicioService.crear(formulario);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorGeneral", e.getMessage());

            prepararFormulario(model, authentication, false, null);

            return "admin/servicios/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El servicio se registró correctamente.");

        return "redirect:/admin/servicios";
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        try {

            model.addAttribute(
                    "servicioForm",
                    servicioService.obtenerFormulario(id));

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el servicio.");
        }

        model.addAttribute("servicioId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("paginaAdmin", "servicios");

        agregarAdministrador(model, authentication);

        return "admin/servicios/formulario";
    }

    // ACTUALIZAR SERVICIO
    @PostMapping("/{id}")
    public String actualizarServicio(
            @PathVariable Long id,
            @Valid @ModelAttribute("servicioForm") ServicioForm formulario,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            servicioService.obtenerFormulario(id);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el servicio.");
        }

        if (bindingResult.hasErrors()) {

            prepararFormulario(model, authentication, true, id);

            return "admin/servicios/formulario";
        }

        try {

            servicioService.actualizar(id, formulario);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorGeneral", e.getMessage());

            prepararFormulario(model, authentication, true, id);

            return "admin/servicios/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El servicio se actualizó correctamente.");

        return "redirect:/admin/servicios";
    }

    // CAMBIAR ESTADO DEL SERVICIO
    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        try {

            servicioService.cambiarEstado(id, activo);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    activo
                            ? "El servicio se activó correctamente."
                            : "El servicio se desactivó correctamente.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage());
        }

        return "redirect:/admin/servicios";
    }

    // DATOS DEL ADMINISTRADOR
    private void agregarAdministrador(
            Model model,
            Authentication authentication) {

        String nombre = usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .map(Usuario::getNombreCompleto)
                .orElse("Administrador");

        model.addAttribute("nombreAdministrador", nombre);
    }

    // CONTEXTO DEL FORMULARIO
    private void prepararFormulario(
            Model model,
            Authentication authentication,
            boolean modoEdicion,
            Long servicioId) {

        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("servicioId", servicioId);
        model.addAttribute("paginaAdmin", "servicios");

        agregarAdministrador(model, authentication);
    }
}