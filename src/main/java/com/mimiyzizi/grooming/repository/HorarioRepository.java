package com.mimiyzizi.grooming.repository;

import com.mimiyzizi.grooming.domain.Horario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

        // CONSULTA ADMINISTRATIVA
        @Query("""
                        SELECT h
                        FROM Horario h
                        WHERE
                            (:fecha IS NULL OR h.fecha = :fecha)
                            AND
                            (:habilitado IS NULL OR h.habilitado = :habilitado)
                        """)
        Page<Horario> buscarParaAdministracion(
                        @Param("fecha") LocalDate fecha,
                        @Param("habilitado") Boolean habilitado,
                        Pageable pageable);

        // HORARIOS FUTUROS HABILITADOS PARA CLIENTES
        @Query("""
                        SELECT h
                        FROM Horario h
                        WHERE h.habilitado = true
                          AND (
                                h.fecha > :fechaActual
                                OR (
                                    h.fecha = :fechaActual
                                    AND h.horaInicio > :horaActual
                                )
                          )
                        ORDER BY h.fecha ASC, h.horaInicio ASC
                        """)
        List<Horario> buscarHorariosFuturos(
                        @Param("fechaActual") LocalDate fechaActual,
                        @Param("horaActual") LocalTime horaActual);

        // COMPROBAR SOLAPAMIENTOS AL CREAR
        boolean existsByFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                        LocalDate fecha,
                        LocalTime horaFin,
                        LocalTime horaInicio);

        // COMPROBAR SOLAPAMIENTOS AL EDITAR
        boolean existsByFechaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                        LocalDate fecha,
                        LocalTime horaFin,
                        LocalTime horaInicio,
                        Long id);

        // CONTADORES
        long countByHabilitadoTrue();

        long countByHabilitadoFalse();

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT h FROM Horario h WHERE h.id = :id")
        Optional<Horario> buscarPorIdConBloqueo(@Param("id") Long id);
}