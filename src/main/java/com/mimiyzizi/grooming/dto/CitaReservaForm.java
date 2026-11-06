package com.mimiyzizi.grooming.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

public class CitaReservaForm {

    @NotNull(message = "No se ha seleccionado un horario.")
    @Positive(message = "El horario seleccionado no es válido.")
    private Long horarioId;

    @NotNull(message = "Selecciona una mascota.")
    @Positive(message = "La mascota seleccionada no es válida.")
    private Long mascotaId;

    @NotEmpty(message = "Selecciona al menos un servicio.")
    private List<@NotNull @Positive Long> serviciosIds = new ArrayList<>();

    public Long getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Long horarioId) {
        this.horarioId = horarioId;
    }

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    public List<Long> getServiciosIds() {
        return serviciosIds;
    }

    public void setServiciosIds(List<Long> serviciosIds) {
        this.serviciosIds = serviciosIds;
    }
}