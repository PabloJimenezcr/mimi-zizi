package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.HorarioForm;
import com.mimiyzizi.grooming.repository.UsuarioRepository;
import com.mimiyzizi.grooming.service.HorarioService;
import java.time.ZoneId;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/horarios")
public class HorarioAdminController {

    private static final int TAMANO_PAGINA = 10;

    private final HorarioService horarioService;
    private final UsuarioRepository usuarioRepository;

    public HorarioAdminController(
            HorarioService horarioService,
            UsuarioRepository usuarioRepository) {

        this.horarioService = horarioService;
        this.usuarioRepository = usuarioRepository;
    }

    // LISTAR HORARIOS
    @GetMapping
    public String listarHorarios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            @RequestParam(defaultValue = "todos") String estado,

            @RequestParam(defaultValue = "0") int page,

            Authentication authentication,
            Model model) {

        String filtroEstado = switch (estado) {
            case "habilitados", "bloqueados" -> estado;
            default -> "todos";
        };

        Boolean habilitado = switch (filtroEstado) {
            case "habilitados" -> true;
            case "bloqueados" -> false;
            default -> null;
        };

        int paginaSolicitada = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                paginaSolicitada,
                TAMANO_PAGINA,
                Sort.by(
                        Sort.Order.desc("fecha"),
                        Sort.Order.asc("horaInicio")));

        Page<com.mimiyzizi.grooming.dto.HorarioDTO> paginaHorarios = horarioService.buscarAdministracion(
                fecha,
                habilitado,
                pageable);

        // Ajustar cuando el filtro deja una página fuera de rango.
        if (paginaHorarios.getTotalPages() > 0
                && paginaSolicitada >= paginaHorarios.getTotalPages()) {

            paginaSolicitada = paginaHorarios.getTotalPages() - 1;

            pageable = PageRequest.of(
                    paginaSolicitada,
                    TAMANO_PAGINA,
                    Sort.by(
                            Sort.Order.desc("fecha"),
                            Sort.Order.asc("horaInicio")));

            paginaHorarios = horarioService.buscarAdministracion(
                    fecha,
                    habilitado,
                    pageable);
        }

        model.addAttribute("horarios", paginaHorarios.getContent());

        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("estado", filtroEstado);

        model.addAttribute("paginaActual", paginaHorarios.getNumber());
        model.addAttribute("totalPaginas", paginaHorarios.getTotalPages());

        model.addAttribute("hayAnterior", paginaHorarios.hasPrevious());
        model.addAttribute("haySiguiente", paginaHorarios.hasNext());

        model.addAttribute("totalHorarios", horarioService.contarHorarios());
        model.addAttribute("horariosHabilitados", horarioService.contarHabilitados());
        model.addAttribute("horariosBloqueados", horarioService.contarBloqueados());

        long desde = paginaHorarios.getTotalElements() == 0
                ? 0
                : (long) paginaHorarios.getNumber() * TAMANO_PAGINA + 1;

        long hasta = desde == 0
                ? 0
                : desde + paginaHorarios.getNumberOfElements() - 1;

        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        // Total de horarios que coinciden con los filtros.
        model.addAttribute(
                "totalFiltrados",
                paginaHorarios.getTotalElements());

        model.addAttribute("paginaAdmin", "horarios");


        agregarAdministrador(model, authentication);

        return "admin/horarios/listado";
    }

    // MOSTRAR FORMULARIO DE CREACIÓN
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(
            Authentication authentication,
            Model model) {

        model.addAttribute("horarioForm", new HorarioForm());
        model.addAttribute("modoEdicion", false);
        model.addAttribute("paginaAdmin", "horarios");

        model.addAttribute(
                "fechaMinima",
                LocalDate.now(ZoneId.of("America/Costa_Rica")));

        agregarAdministrador(model, authentication);

        return "admin/horarios/formulario";
    }

    // CREAR HORARIO
    @PostMapping
    public String crearHorario(
            @Valid @ModelAttribute("horarioForm") HorarioForm formulario,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            prepararFormulario(model, authentication, false, null);

            return "admin/horarios/formulario";
        }

        try {

            horarioService.crear(formulario);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorGeneral", e.getMessage());

            prepararFormulario(model, authentication, false, null);

            return "admin/horarios/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El horario se registró correctamente.");

        return "redirect:/admin/horarios";
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        try {

            model.addAttribute(
                    "horarioForm",
                    horarioService.obtenerFormulario(id));

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el horario.");
        }

        model.addAttribute("horarioId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("paginaAdmin", "horarios");

        model.addAttribute(
                "fechaMinima",
                LocalDate.now(ZoneId.of("America/Costa_Rica")));

        agregarAdministrador(model, authentication);

        return "admin/horarios/formulario";
    }

    // ACTUALIZAR HORARIO
    @PostMapping("/{id}")
    public String actualizarHorario(
            @PathVariable Long id,
            @Valid @ModelAttribute("horarioForm") HorarioForm formulario,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            horarioService.obtenerFormulario(id);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el horario.");
        }

        if (bindingResult.hasErrors()) {

            prepararFormulario(model, authentication, true, id);

            return "admin/horarios/formulario";
        }

        try {

            horarioService.actualizar(id, formulario);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorGeneral", e.getMessage());

            prepararFormulario(model, authentication, true, id);

            return "admin/horarios/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El horario se actualizó correctamente.");

        return "redirect:/admin/horarios";
    }

    // HABILITAR O BLOQUEAR
    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean habilitado,
            RedirectAttributes redirectAttributes) {

        try {

            horarioService.cambiarEstado(id, habilitado);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    habilitado
                            ? "El horario se habilitó correctamente."
                            : "El horario se bloqueó correctamente.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage());
        }

        return "redirect:/admin/horarios";
    }

    // ADMINISTRADOR
    private void agregarAdministrador(
            Model model,
            Authentication authentication) {

        String nombre = usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .map(Usuario::getNombreCompleto)
                .orElse("Administrador");

        model.addAttribute("nombreAdministrador", nombre);
    }

    private void prepararFormulario(
            Model model,
            Authentication authentication,
            boolean modoEdicion,
            Long horarioId) {

        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("horarioId", horarioId);
        model.addAttribute("paginaAdmin", "horarios");

        model.addAttribute(
                "fechaMinima",
                LocalDate.now(ZoneId.of("America/Costa_Rica")));

        agregarAdministrador(model, authentication);
    }
}