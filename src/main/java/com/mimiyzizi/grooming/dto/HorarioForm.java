package com.mimiyzizi.grooming.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioForm {

    @NotNull(message = "Debes seleccionar una fecha.")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.")
    private LocalDate fecha;

    @NotNull(message = "Debes seleccionar la hora de inicio.")
    private LocalTime horaInicio;

    @NotNull(message = "Debes seleccionar la hora de finalización.")
    private LocalTime horaFin;

    @NotNull(message = "Debes indicar la capacidad.")
    @Min(value = 1, message = "La capacidad mínima es de 1 mascota.")
    @Max(value = 65535, message = "La capacidad supera el límite permitido.")
    private Integer capacidad;

    private boolean habilitado = true;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }
}