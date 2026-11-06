package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.RolUsuario;
import com.mimiyzizi.grooming.domain.Usuario;

import java.time.format.DateTimeFormatter;

public record UsuarioAdminDTO(
        Long id,
        String nombreCompleto,
        String correo,
        String telefono,
        RolUsuario rol,
        boolean activo,
        String fechaRegistro) {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static UsuarioAdminDTO desde(Usuario usuario) {

        String fecha = usuario.getFechaRegistro() != null
                ? usuario.getFechaRegistro().format(FORMATO_FECHA)
                : "No disponible";

        return new UsuarioAdminDTO(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.isActivo(),
                fecha);
    }
}