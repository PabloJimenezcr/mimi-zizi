package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.Horario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record HorarioDisponibilidadDTO(
        Long id,
        LocalDate fechaValor,
        String fecha,
        String horaInicio,
        String horaFin,
        long citasDisponibles,
        boolean completo) {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static HorarioDisponibilidadDTO desde(
            Horario horario,
            long citasDisponibles) {

        return new HorarioDisponibilidadDTO(
                horario.getId(),
                horario.getFecha(),
                horario.getFecha().format(FORMATO_FECHA),
                horario.getHoraInicio().format(FORMATO_HORA),
                horario.getHoraFin().format(FORMATO_HORA),
                citasDisponibles,
                citasDisponibles == 0);
    }
}