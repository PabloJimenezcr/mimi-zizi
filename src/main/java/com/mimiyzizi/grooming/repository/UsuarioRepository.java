package com.mimiyzizi.grooming.repository;

import com.mimiyzizi.grooming.domain.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar un usuario por correo electrónico
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Verificar si un correo ya está registrado
    boolean existsByCorreoIgnoreCase(String correo);

    // Buscar usuarios por nombre, correo o teléfono con paginación
    Page<Usuario> findByNombreCompletoContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
            String nombre,
            String correo,
            String telefono,
            Pageable pageable
    );
}