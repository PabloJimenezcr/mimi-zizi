package com.mimiyzizi.grooming.repository;

import com.mimiyzizi.grooming.domain.Cita;
import com.mimiyzizi.grooming.domain.EstadoCita;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Contar las citas de un horario, excluyendo las canceladas.
    long countByHorario_IdAndEstadoNot(
            Long horarioId,
            EstadoCita estado);

    // Buscar una cita específica que pertenezca al cliente.
    // También carga la mascota para mostrarla en la confirmación.
    @EntityGraph(attributePaths = { "mascota" })
    Optional<Cita> findByIdAndUsuario_Id(
            Long id,
            Long usuarioId);

}