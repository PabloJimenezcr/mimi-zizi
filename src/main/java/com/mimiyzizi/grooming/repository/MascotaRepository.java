package com.mimiyzizi.grooming.repository;

import com.mimiyzizi.grooming.domain.Mascota;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Consultar las mascotas de un cliente.
    List<Mascota> findAllByUsuario_IdOrderByFechaRegistroDesc(Long usuarioId);

    // Buscar una mascota verificando también su propietario.
    Optional<Mascota> findByIdAndUsuario_Id(Long id, Long usuarioId);

    // Contar las mascotas registradas por un cliente.
    long countByUsuario_Id(Long usuarioId);
}