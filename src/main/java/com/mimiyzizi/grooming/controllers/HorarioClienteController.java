package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.dto.HorarioDisponibilidadDTO;
import com.mimiyzizi.grooming.service.HorarioService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/cliente/horarios")
public class HorarioClienteController {

    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final HorarioService horarioService;

    public HorarioClienteController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    // CONSULTAR HORARIOS POR FECHA
    @GetMapping
    public String consultarHorarios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            Model model) {

        LocalDate hoy = LocalDate.now(ZONA_COSTA_RICA);

        /*
         * Obtenemos las próximas siete fechas que
         * tengan al menos una cita disponible.
         */
        List<LocalDate> fechasDisponibles = horarioService.listarFechasConDisponibilidad(7);

        /*
         * Si el cliente no seleccionó una fecha,
         * mostramos automáticamente la más próxima
         * que tenga disponibilidad.
         *
         * Si seleccionó una fecha, respetamos su elección.
         */
        LocalDate fechaSeleccionada = fecha != null
                ? fecha
                : fechasDisponibles.stream()
                        .findFirst()
                        .orElse(null);

        /*
         * Solo consultamos los horarios correspondientes
         * al día seleccionado.
         */
        List<HorarioDisponibilidadDTO> horarios = fechaSeleccionada == null
                || fechaSeleccionada.isBefore(hoy)
                        ? List.of()
                        : horarioService.listarDisponibilidadPorFecha(
                                fechaSeleccionada);

        model.addAttribute("horarios", horarios);
        model.addAttribute("fechasDisponibles", fechasDisponibles);

        boolean hayCitasDisponibles = horarios.stream()
                .anyMatch(horario -> !horario.completo());

        model.addAttribute("hayCitasDisponibles", hayCitasDisponibles);

        model.addAttribute("fechaSeleccionada", fechaSeleccionada);
        model.addAttribute("fechaMinima", hoy);

        model.addAttribute(
                "hayFechasDisponibles",
                !fechasDisponibles.isEmpty());

        model.addAttribute(
                "hayHorarios",
                !horarios.isEmpty());

        model.addAttribute("paginaCliente", "horarios");

        return "cliente/horarios/disponibles";
    }
}