package com.mimiyzizi.grooming.repository;

import com.mimiyzizi.grooming.domain.Servicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // Servicios visibles para los clientes.
    List<Servicio> findAllByActivoTrueOrderByNombreAsc();

    // Comprobar nombres duplicados al crear.
    boolean existsByNombreIgnoreCase(String nombre);

    // Comprobar nombres duplicados al editar, excluyendo el propio servicio.
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    // Contar servicios activos e inactivos.
    long countByActivoTrue();

    long countByActivoFalse();

    // Búsqueda administrativa con filtros opcionales.
    @Query("""
            SELECT s
            FROM Servicio s
            WHERE
                (
                    :busqueda IS NULL
                    OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                    OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                )
                AND
                (
                    :activo IS NULL
                    OR s.activo = :activo
                )
            """)
    Page<Servicio> buscarParaAdministracion(
            @Param("busqueda") String busqueda,
            @Param("activo") Boolean activo,
            Pageable pageable);

    // Buscar un servicio específico.
    Optional<Servicio> findById(Long id);

    // Obtener únicamente los servicios seleccionados que estén activos.
    List<Servicio> findAllByIdInAndActivoTrue(Collection<Long> ids);
}