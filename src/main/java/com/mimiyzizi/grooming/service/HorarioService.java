package com.mimiyzizi.grooming.service;

import com.mimiyzizi.grooming.domain.EstadoCita;
import com.mimiyzizi.grooming.domain.Horario;
import com.mimiyzizi.grooming.dto.HorarioDTO;
import com.mimiyzizi.grooming.dto.HorarioDisponibilidadDTO;
import com.mimiyzizi.grooming.dto.HorarioForm;
import com.mimiyzizi.grooming.repository.CitaRepository;
import com.mimiyzizi.grooming.repository.HorarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HorarioService {

    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final HorarioRepository horarioRepository;
    private final CitaRepository citaRepository;

    public HorarioService(
            HorarioRepository horarioRepository,
            CitaRepository citaRepository) {

        this.horarioRepository = horarioRepository;
        this.citaRepository = citaRepository;
    }

    // =====================================================
    // ADMINISTRACIÓN DE HORARIOS
    // =====================================================

    @Transactional(readOnly = true)
    public Page<HorarioDTO> buscarAdministracion(
            LocalDate fecha,
            Boolean habilitado,
            Pageable pageable) {

        return horarioRepository
                .buscarParaAdministracion(fecha, habilitado, pageable)
                .map(HorarioDTO::desde);
    }

    // =====================================================
    // CONSULTA ANTIGUA DE HORARIOS
    // Se conserva para mantener compatibilidad.
    // =====================================================

    @Transactional(readOnly = true)
    public List<HorarioDTO> listarDisponibles() {

        LocalDate hoy = LocalDate.now(ZONA_COSTA_RICA);
        LocalTime horaActual = LocalTime.now(ZONA_COSTA_RICA);

        return horarioRepository
                .buscarHorariosFuturos(hoy, horaActual)
                .stream()
                .map(HorarioDTO::desde)
                .toList();
    }

    // =====================================================
    // NUEVA EXPERIENCIA DE DISPONIBILIDAD PARA CLIENTES
    // =====================================================

    /**
     * Encuentra automáticamente la próxima fecha
     * que tenga al menos una cita disponible.
     */
    @Transactional(readOnly = true)
    public Optional<LocalDate> obtenerProximaFechaConDisponibilidad() {

        List<Horario> horarios = obtenerHorariosFuturos();

        for (Horario horario : horarios) {

            long disponibles = calcularCitasDisponibles(horario);

            if (disponibles > 0) {
                return Optional.of(horario.getFecha());
            }
        }

        return Optional.empty();
    }

    /**
     * Devuelve los horarios futuros de una fecha específica.
     *
     * Los horarios completos también se incluyen,
     * pero se identifican para mostrarlos deshabilitados.
     */
    @Transactional(readOnly = true)
    public List<HorarioDisponibilidadDTO> listarDisponibilidadPorFecha(
            LocalDate fecha) {

        if (fecha == null) {
            return List.of();
        }

        return obtenerHorariosFuturos()
                .stream()
                .filter(horario -> horario.getFecha().isEqual(fecha))
                .map(horario -> {

                    long disponibles = calcularCitasDisponibles(horario);

                    return HorarioDisponibilidadDTO.desde(
                            horario,
                            disponibles);

                })
                .toList();
    }

    /**
     * Obtiene las próximas fechas que tienen al menos
     * un horario con citas disponibles.
     *
     * El límite permite mostrar solamente algunas
     * fechas cercanas en la interfaz.
     */
    @Transactional(readOnly = true)
    public List<LocalDate> listarFechasConDisponibilidad(int limite) {

        if (limite <= 0) {
            return List.of();
        }

        Set<LocalDate> fechas = new LinkedHashSet<>();

        for (Horario horario : obtenerHorariosFuturos()) {

            if (calcularCitasDisponibles(horario) > 0) {

                fechas.add(horario.getFecha());

                if (fechas.size() >= limite) {
                    break;
                }
            }
        }

        return List.copyOf(fechas);
    }

    /**
     * Carga todos los horarios habilitados y futuros
     * utilizando la fecha y hora de Costa Rica.
     */
    private List<Horario> obtenerHorariosFuturos() {

        LocalDate hoy = LocalDate.now(ZONA_COSTA_RICA);
        LocalTime horaActual = LocalTime.now(ZONA_COSTA_RICA);

        return horarioRepository.buscarHorariosFuturos(
                hoy,
                horaActual);
    }

    /**
     * Calcula cuántas citas quedan en un horario.
     * Las citas canceladas no consumen capacidad.
     */
    private long calcularCitasDisponibles(Horario horario) {

        long citasRegistradas = citaRepository.countByHorario_IdAndEstadoNot(
                horario.getId(),
                EstadoCita.CANCELADA);

        return Math.max(
                0,
                (long) horario.getCapacidad() - citasRegistradas);
    }

    // =====================================================
    // OBTENER DATOS PARA EDITAR
    // =====================================================

    @Transactional(readOnly = true)
    public HorarioForm obtenerFormulario(Long id) {

        Horario horario = obtenerHorario(id);

        HorarioForm formulario = new HorarioForm();

        formulario.setFecha(horario.getFecha());
        formulario.setHoraInicio(horario.getHoraInicio());
        formulario.setHoraFin(horario.getHoraFin());
        formulario.setCapacidad(horario.getCapacidad());
        formulario.setHabilitado(horario.isHabilitado());

        return formulario;
    }

    // =====================================================
    // CREAR HORARIO
    // =====================================================

    @Transactional
    public void crear(HorarioForm formulario) {

        validarHorario(formulario, null);

        Horario horario = new Horario();

        horario.setFecha(formulario.getFecha());
        horario.setHoraInicio(formulario.getHoraInicio());
        horario.setHoraFin(formulario.getHoraFin());
        horario.setCapacidad(formulario.getCapacidad());

        // Los horarios nuevos comienzan habilitados.
        horario.setHabilitado(true);

        horarioRepository.save(horario);
    }

    // =====================================================
    // ACTUALIZAR HORARIO
    // =====================================================

    @Transactional
    public void actualizar(Long id, HorarioForm formulario) {

        Horario horario = obtenerHorario(id);

        validarHorario(formulario, id);

        horario.setFecha(formulario.getFecha());
        horario.setHoraInicio(formulario.getHoraInicio());
        horario.setHoraFin(formulario.getHoraFin());
        horario.setCapacidad(formulario.getCapacidad());

        // El estado se administra mediante otra operación.
        horarioRepository.save(horario);
    }

    // =====================================================
    // HABILITAR O BLOQUEAR HORARIO
    // =====================================================

    @Transactional
    public void cambiarEstado(Long id, boolean habilitado) {

        Horario horario = obtenerHorario(id);

        horario.setHabilitado(habilitado);

        horarioRepository.save(horario);
    }

    // =====================================================
    // INDICADORES ADMINISTRATIVOS
    // =====================================================

    @Transactional(readOnly = true)
    public long contarHorarios() {
        return horarioRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarHabilitados() {
        return horarioRepository.countByHabilitadoTrue();
    }

    @Transactional(readOnly = true)
    public long contarBloqueados() {
        return horarioRepository.countByHabilitadoFalse();
    }

    // =====================================================
    // VALIDACIONES DE NEGOCIO
    // =====================================================

    private void validarHorario(
            HorarioForm formulario,
            Long idExcluir) {

        LocalDate fecha = formulario.getFecha();
        LocalTime inicio = formulario.getHoraInicio();
        LocalTime fin = formulario.getHoraFin();
        Integer capacidad = formulario.getCapacidad();

        if (fecha == null || inicio == null
                || fin == null || capacidad == null) {

            throw new IllegalArgumentException(
                    "Todos los campos obligatorios deben estar completos.");
        }

        LocalDate hoy = LocalDate.now(ZONA_COSTA_RICA);
        LocalTime horaActual = LocalTime.now(ZONA_COSTA_RICA);

        if (fecha.isBefore(hoy)) {
            throw new IllegalArgumentException(
                    "No puedes registrar horarios en fechas pasadas.");
        }

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La hora de finalización debe ser posterior a la hora de inicio.");
        }

        if (fecha.isEqual(hoy) && !inicio.isAfter(horaActual)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser posterior a la hora actual.");
        }

        if (capacidad <= 0 || capacidad > 65535) {
            throw new IllegalArgumentException(
                    "La cantidad de citas debe estar entre 1 y 65535.");
        }

        boolean existeCruce = idExcluir == null
                ? horarioRepository
                        .existsByFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                                fecha,
                                fin,
                                inicio)
                : horarioRepository
                        .existsByFechaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                                fecha,
                                fin,
                                inicio,
                                idExcluir);

        if (existeCruce) {
            throw new IllegalArgumentException(
                    "El horario se cruza con otro bloque registrado para esa fecha.");
        }
    }

    // =====================================================
    // OBTENER POR ID
    // =====================================================

    private Horario obtenerHorario(Long id) {

        return horarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el horario solicitado."));
    }
}