package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.Mascota;

import java.time.format.DateTimeFormatter;

public record MascotaDTO(
        Long id,
        String nombre,
        String raza,
        String tamano,
        String observaciones,
        String fechaRegistro,
        boolean tieneFoto
) {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static MascotaDTO desde(Mascota mascota) {

        String nombreTamano = switch (mascota.getTamano()) {
            case PEQUENO -> "Pequeño";
            case MEDIANO -> "Mediano";
            case GRANDE -> "Grande";
        };

        String fecha = mascota.getFechaRegistro() != null
                ? mascota.getFechaRegistro().format(FORMATO_FECHA)
                : "No disponible";

        return new MascotaDTO(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getRaza(),
                nombreTamano,
                mascota.getObservaciones(),
                fecha,
                mascota.getFotoNombre() != null
                        && !mascota.getFotoNombre().isBlank()
        );
    }
}