package com.mimiyzizi.grooming.service;

import com.mimiyzizi.grooming.domain.Cita;
import com.mimiyzizi.grooming.domain.DetalleCita;
import com.mimiyzizi.grooming.domain.EstadoCita;
import com.mimiyzizi.grooming.domain.Horario;
import com.mimiyzizi.grooming.domain.Mascota;
import com.mimiyzizi.grooming.domain.Servicio;
import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.CitaReservaForm;
import com.mimiyzizi.grooming.repository.CitaRepository;
import com.mimiyzizi.grooming.repository.HorarioRepository;
import com.mimiyzizi.grooming.repository.MascotaRepository;
import com.mimiyzizi.grooming.repository.ServicioRepository;
import com.mimiyzizi.grooming.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class CitaService {

    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final CitaRepository citaRepository;
    private final HorarioRepository horarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ServicioRepository servicioRepository;

    public CitaService(
            CitaRepository citaRepository,
            HorarioRepository horarioRepository,
            UsuarioRepository usuarioRepository,
            MascotaRepository mascotaRepository,
            ServicioRepository servicioRepository) {

        this.citaRepository = citaRepository;
        this.horarioRepository = horarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.servicioRepository = servicioRepository;
    }

    /**
     * Comprueba que el horario se pueda ofrecer al cliente.
     */
    @Transactional(readOnly = true)
    public Horario obtenerHorarioDisponible(Long horarioId) {

        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El horario solicitado no existe."));

        validarHorarioVigente(horario);

        long citasRegistradas = citaRepository.countByHorario_IdAndEstadoNot(
                horario.getId(),
                EstadoCita.CANCELADA);

        if (citasRegistradas >= horario.getCapacidad()) {
            throw new IllegalArgumentException(
                    "Este horario ya no tiene espacios disponibles. "
                            + "Selecciona otra jornada.");
        }

        return horario;
    }

    /**
     * Registra una nueva cita y sus servicios.
     */

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long crearReserva(String correoCliente, CitaReservaForm formulario) {

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correoCliente)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo identificar tu cuenta."));

        /*
         * Bloqueamos el horario dentro de la transacción.
         * Así evitamos superar su capacidad si llegan dos reservas
         * al mismo tiempo.
         */
        Horario horario = horarioRepository
                .buscarPorIdConBloqueo(formulario.getHorarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El horario seleccionado ya no existe."));

        validarHorarioVigente(horario);

        long citasRegistradas = citaRepository.countByHorario_IdAndEstadoNot(
                horario.getId(),
                EstadoCita.CANCELADA);

        if (citasRegistradas >= horario.getCapacidad()) {
            throw new IllegalArgumentException(
                    "Lo sentimos, otra persona acaba de ocupar "
                            + "el último espacio de este horario. "
                            + "Selecciona otra fecha.");
        }

        /*
         * La mascota debe pertenecer al cliente autenticado.
         */
        Mascota mascota = mascotaRepository
                .findByIdAndUsuario_Id(
                        formulario.getMascotaId(),
                        usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La mascota seleccionada no pertenece "
                                + "a tu cuenta."));

        /*
         * Elimina posibles identificadores duplicados.
         */
        List<Long> idsServicios = new LinkedHashSet<>(
                formulario.getServiciosIds()).stream().toList();

        if (idsServicios.size() != formulario.getServiciosIds().size()) {
            throw new IllegalArgumentException(
                    "Hay servicios repetidos en tu selección.");
        }

        /*
         * Recuperamos exclusivamente los servicios activos.
         */
        List<Servicio> servicios = servicioRepository.findAllByIdInAndActivoTrue(idsServicios);

        if (servicios.size() != idsServicios.size()) {
            throw new IllegalArgumentException(
                    "Uno de los servicios seleccionados ya no está disponible. "
                            + "Actualiza tu selección.");
        }

        /*
         * Creamos la cita y guardamos una copia de la fecha
         * y las horas del horario seleccionado.
         */
        Cita cita = new Cita();

        cita.setUsuario(usuario);
        cita.setMascota(mascota);
        cita.setHorario(horario);
        cita.setFechaCita(horario.getFecha());
        cita.setHoraInicio(horario.getHoraInicio());
        cita.setHoraFin(horario.getHoraFin());
        cita.setEstado(EstadoCita.PENDIENTE);

        /*
         * Guardamos el precio actual de cada servicio.
         * El valor se obtiene desde la base de datos,
         * nunca desde el formulario del cliente.
         */
        for (Servicio servicio : servicios) {

            DetalleCita detalle = new DetalleCita();

            detalle.setServicio(servicio);
            detalle.setPrecioAplicado(servicio.getPrecio());

            cita.agregarDetalle(detalle);
        }

        Cita citaGuardada = citaRepository.save(cita);

        return citaGuardada.getId();
    }

    /**
     * Validaciones del horario en la zona horaria de Costa Rica.
     */
    private void validarHorarioVigente(Horario horario) {

        if (!horario.isHabilitado()) {
            throw new IllegalArgumentException(
                    "Este horario ya no está habilitado.");
        }

        LocalDate hoy = LocalDate.now(ZONA_COSTA_RICA);
        LocalTime horaActual = LocalTime.now(ZONA_COSTA_RICA);

        boolean esFuturo = horario.getFecha().isAfter(hoy)
                || (horario.getFecha().isEqual(hoy)
                        && horario.getHoraInicio().isAfter(horaActual));

        if (!esFuturo) {
            throw new IllegalArgumentException(
                    "Este horario ya pasó. Selecciona una fecha futura.");
        }
    }
}