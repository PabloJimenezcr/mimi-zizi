package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.Servicio;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record ServicioDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer duracionMinutos,
        boolean activo,
        String fechaRegistro) {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ServicioDTO desde(Servicio servicio) {

        String fecha = servicio.getFechaRegistro() != null
                ? servicio.getFechaRegistro().format(FORMATO_FECHA)
                : "No disponible";

        return new ServicioDTO(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio(),
                servicio.getDuracionMinutos(),
                servicio.isActivo(),
                fecha);
    }
}