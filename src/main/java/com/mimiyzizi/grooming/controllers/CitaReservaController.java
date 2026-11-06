
package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.domain.Cita;
import com.mimiyzizi.grooming.domain.Mascota;
import com.mimiyzizi.grooming.domain.Servicio;
import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.CitaReservaForm;
import com.mimiyzizi.grooming.dto.HorarioDTO;
import com.mimiyzizi.grooming.repository.CitaRepository;
import com.mimiyzizi.grooming.repository.MascotaRepository;
import com.mimiyzizi.grooming.repository.ServicioRepository;
import com.mimiyzizi.grooming.repository.UsuarioRepository;
import com.mimiyzizi.grooming.service.CitaService;

import jakarta.validation.Valid;

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

import java.util.List;

@Controller
@RequestMapping("/cliente/reservas")
public class CitaReservaController {

    private static final String VISTA_FORMULARIO = "cliente/reservas/formulario";

    private final CitaService citaService;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ServicioRepository servicioRepository;

    public CitaReservaController(
            CitaService citaService,
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            MascotaRepository mascotaRepository,
            ServicioRepository servicioRepository) {

        this.citaService = citaService;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.servicioRepository = servicioRepository;
    }

    // MOSTRAR FORMULARIO DE RESERVA
    @GetMapping("/nueva")
    public String mostrarFormulario(
            @RequestParam Long horarioId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            agregarDatosFormulario(model, authentication, horarioId);

            CitaReservaForm formulario = new CitaReservaForm();
            formulario.setHorarioId(horarioId);

            model.addAttribute("reservaForm", formulario);

            return VISTA_FORMULARIO;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage());

            return "redirect:/cliente/horarios";
        }
    }

    // REGISTRAR RESERVA
    @PostMapping
    public String crearReserva(
            @Valid @ModelAttribute("reservaForm") CitaReservaForm formulario,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            if (!prepararFormulario(
                    model, authentication, formulario.getHorarioId())) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "El horario seleccionado ya no está disponible.");

                return "redirect:/cliente/horarios";
            }

            return VISTA_FORMULARIO;
        }

        try {

            Long citaId = citaService.crearReserva(
                    authentication.getName(),
                    formulario);

            redirectAttributes.addFlashAttribute(
                    "reservaCreada",
                    true);

            return "redirect:/cliente/reservas/confirmacion/" + citaId;

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorGeneral", e.getMessage());

            if (!prepararFormulario(
                    model, authentication, formulario.getHorarioId())) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        e.getMessage());

                return "redirect:/cliente/horarios";
            }

            return VISTA_FORMULARIO;
        }
    }

    // CONFIRMACIÓN DE LA RESERVA
    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Usuario usuario = obtenerUsuario(authentication);

        Cita cita = citaRepository
                .findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la reserva solicitada."));

        model.addAttribute("cita", cita);
        model.addAttribute("nombreCliente", usuario.getNombreCompleto());

        return "cliente/reservas/confirmacion";
    }

    // PREPARAR INFORMACIÓN DEL FORMULARIO
    private boolean prepararFormulario(
            Model model,
            Authentication authentication,
            Long horarioId) {

        if (horarioId == null) {
            return false;
        }

        try {
            agregarDatosFormulario(model, authentication, horarioId);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void agregarDatosFormulario(
            Model model,
            Authentication authentication,
            Long horarioId) {

        Usuario usuario = obtenerUsuario(authentication);

        model.addAttribute(
                "horario",
                HorarioDTO.desde(
                        citaService.obtenerHorarioDisponible(horarioId)));

        List<Mascota> mascotas = mascotaRepository.findAllByUsuario_IdOrderByFechaRegistroDesc(
                usuario.getId());

        List<Servicio> servicios = servicioRepository.findAllByActivoTrueOrderByNombreAsc();

        model.addAttribute("mascotas", mascotas);
        model.addAttribute("servicios", servicios);
        model.addAttribute("nombreCliente", usuario.getNombreCompleto());
    }

    private Usuario obtenerUsuario(Authentication authentication) {

        return usuarioRepository
                .findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo identificar tu cuenta."));
    }
}
