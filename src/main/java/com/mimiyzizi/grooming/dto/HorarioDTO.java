package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.Horario;

import java.time.format.DateTimeFormatter;

public record HorarioDTO(
        Long id,
        String fecha,
        String horaInicio,
        String horaFin,
        Integer capacidad,
        boolean habilitado,
        String fechaRegistro) {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_REGISTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static HorarioDTO desde(Horario horario) {

        String fechaFormateada = horario.getFecha()
                .format(FORMATO_FECHA);

        String inicio = horario.getHoraInicio()
                .format(FORMATO_HORA);

        String fin = horario.getHoraFin()
                .format(FORMATO_HORA);

        String registro = horario.getFechaRegistro() != null
                ? horario.getFechaRegistro().format(FORMATO_REGISTRO)
                : "No disponible";

        return new HorarioDTO(
                horario.getId(),
                fechaFormateada,
                inicio,
                fin,
                horario.getCapacidad(),
                horario.isHabilitado(),
                registro);
    }
}