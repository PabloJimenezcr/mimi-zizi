package com.mimiyzizi.grooming.service;

import com.mimiyzizi.grooming.domain.Servicio;
import com.mimiyzizi.grooming.dto.ServicioDTO;
import com.mimiyzizi.grooming.dto.ServicioForm;
import com.mimiyzizi.grooming.repository.ServicioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    // CONSULTAR TODOS LOS SERVICIOS PARA ADMINISTRACIÓN
    @Transactional(readOnly = true)
    public Page<ServicioDTO> buscarAdministracion(
            String busqueda,
            Boolean activo,
            Pageable pageable) {

        String termino = busqueda == null || busqueda.isBlank()
                ? null
                : busqueda.trim();

        return servicioRepository
                .buscarParaAdministracion(termino, activo, pageable)
                .map(ServicioDTO::desde);
    }

    // CONSULTAR SOLAMENTE LOS SERVICIOS ACTIVOS PARA CLIENTES
    @Transactional(readOnly = true)
    public List<ServicioDTO> listarServiciosActivos() {

        return servicioRepository
                .findAllByActivoTrueOrderByNombreAsc()
                .stream()
                .map(ServicioDTO::desde)
                .toList();
    }

    // OBTENER UN SERVICIO PARA EDITAR
    @Transactional(readOnly = true)
    public ServicioForm obtenerFormulario(Long id) {

        Servicio servicio = obtenerServicio(id);

        ServicioForm formulario = new ServicioForm();

        formulario.setNombre(servicio.getNombre());
        formulario.setDescripcion(servicio.getDescripcion());
        formulario.setPrecio(servicio.getPrecio());
        formulario.setDuracionMinutos(servicio.getDuracionMinutos());
        formulario.setActivo(servicio.isActivo());

        return formulario;
    }

    // REGISTRAR UN NUEVO SERVICIO
    @Transactional
    public void crear(ServicioForm formulario) {

        String nombre = formulario.getNombre().trim();

        validarNombreDuplicado(nombre, null);

        Servicio servicio = new Servicio();

        servicio.setNombre(nombre);
        servicio.setDescripcion(formulario.getDescripcion().trim());
        servicio.setPrecio(formulario.getPrecio());
        servicio.setDuracionMinutos(formulario.getDuracionMinutos());

        // Todo servicio nuevo comienza activo.
        servicio.setActivo(true);

        servicioRepository.save(servicio);
    }

    // ACTUALIZAR LOS DATOS DE UN SERVICIO
    @Transactional
    public void actualizar(Long id, ServicioForm formulario) {

        Servicio servicio = obtenerServicio(id);

        String nombre = formulario.getNombre().trim();

        validarNombreDuplicado(nombre, id);

        servicio.setNombre(nombre);
        servicio.setDescripcion(formulario.getDescripcion().trim());
        servicio.setPrecio(formulario.getPrecio());
        servicio.setDuracionMinutos(formulario.getDuracionMinutos());

        // Conservamos el estado actual.
        // Activar/desactivar se maneja por separado.
        servicioRepository.save(servicio);
    }

    // ACTIVAR O DESACTIVAR SERVICIO
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {

        Servicio servicio = obtenerServicio(id);

        servicio.setActivo(activo);

        servicioRepository.save(servicio);
    }

    // CONTADORES PARA EL PANEL ADMINISTRATIVO
    @Transactional(readOnly = true)
    public long contarServicios() {
        return servicioRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarServiciosActivos() {
        return servicioRepository.countByActivoTrue();
    }

    @Transactional(readOnly = true)
    public long contarServiciosInactivos() {
        return servicioRepository.countByActivoFalse();
    }

    // VALIDAR QUE EL NOMBRE NO ESTÉ REPETIDO
    private void validarNombreDuplicado(String nombre, Long id) {

        boolean existe = id == null
                ? servicioRepository.existsByNombreIgnoreCase(nombre)
                : servicioRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);

        if (existe) {
            throw new IllegalArgumentException(
                    "Ya existe un servicio registrado con ese nombre.");
        }
    }

    // OBTENER SERVICIO POR ID
    private Servicio obtenerServicio(Long id) {

        return servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el servicio solicitado."));
    }
}