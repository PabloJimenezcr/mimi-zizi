package com.mimiyzizi.grooming.service;

import com.mimiyzizi.grooming.domain.Mascota;
import com.mimiyzizi.grooming.domain.Usuario;
import com.mimiyzizi.grooming.dto.MascotaDTO;
import com.mimiyzizi.grooming.dto.MascotaForm;
import com.mimiyzizi.grooming.repository.MascotaRepository;
import com.mimiyzizi.grooming.repository.UsuarioRepository;

import com.mimiyzizi.grooming.dto.MascotaEdicionDTO;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaFotoService mascotaFotoService;

    public MascotaService(
            MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository,
            MascotaFotoService mascotaFotoService) {

        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaFotoService = mascotaFotoService;
    }

    // Obtener los datos de una mascota para editarla.
    @Transactional(readOnly = true)
    public MascotaEdicionDTO obtenerDatosEdicion(
            String correo,
            Long mascotaId) {

        Usuario usuario = obtenerUsuario(correo);

        Mascota mascota = mascotaRepository
                .findByIdAndUsuario_Id(mascotaId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la mascota solicitada."));

        MascotaForm formulario = new MascotaForm();

        formulario.setNombre(mascota.getNombre());
        formulario.setRaza(mascota.getRaza());
        formulario.setTamano(mascota.getTamano());
        formulario.setObservaciones(mascota.getObservaciones());

        boolean tieneFoto = mascota.getFotoNombre() != null
                && !mascota.getFotoNombre().isBlank();

        return new MascotaEdicionDTO(formulario, tieneFoto);
    }

    // Actualizar los datos de una mascota propia.
    @Transactional
    public void actualizarMascota(
            String correo,
            Long mascotaId,
            MascotaForm formulario,
            MultipartFile foto) {

        Usuario usuario = obtenerUsuario(correo);

        Mascota mascota = mascotaRepository
                .findByIdAndUsuario_Id(mascotaId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la mascota solicitada."));

        String fotoAnterior = mascota.getFotoNombre();

        // Guardar una nueva fotografía, si el cliente seleccionó una.
        String fotoNueva = mascotaFotoService.guardar(foto);

        // Actualizar información.
        mascota.setNombre(formulario.getNombre().trim());

        mascota.setRaza(
                formulario.getRaza() == null
                        || formulario.getRaza().isBlank()
                                ? null
                                : formulario.getRaza().trim());

        mascota.setTamano(formulario.getTamano());

        mascota.setObservaciones(
                formulario.getObservaciones() == null
                        || formulario.getObservaciones().isBlank()
                                ? null
                                : formulario.getObservaciones().trim());

        // Una fotografía nueva reemplaza la anterior.
        if (fotoNueva != null) {
            mascota.setFotoNombre(fotoNueva);

        } else if (formulario.isEliminarFoto()) {
            mascota.setFotoNombre(null);
        }

        String fotoFinal = mascota.getFotoNombre();

        boolean eliminarFotoAnterior = fotoAnterior != null
                && !fotoAnterior.equals(fotoFinal);

        /*
         * La limpieza de archivos se ejecuta después de confirmar
         * la transacción. Si la actualización falla, eliminamos
         * la fotografía nueva y conservamos la anterior.
         */
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (fotoNueva != null) {
                mascotaFotoService.eliminar(fotoNueva);
            }

            throw new IllegalStateException(
                    "No se pudo iniciar la operación de actualización.");
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        if (eliminarFotoAnterior) {
                            mascotaFotoService.eliminar(fotoAnterior);
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_COMMITTED && fotoNueva != null) {
                            mascotaFotoService.eliminar(fotoNueva);
                        }
                    }
                });

        mascotaRepository.save(mascota);
    }

    @Transactional(readOnly = true)
    public List<MascotaDTO> listarMisMascotas(String correo) {

        Usuario usuario = obtenerUsuario(correo);

        return mascotaRepository
                .findAllByUsuario_IdOrderByFechaRegistroDesc(usuario.getId())
                .stream()
                .map(MascotaDTO::desde)
                .toList();
    }

    @Transactional
    public void registrarMascota(
            String correo,
            MascotaForm formulario,
            MultipartFile foto) {

        Usuario usuario = obtenerUsuario(correo);

        String fotoNombre = mascotaFotoService.guardar(foto);

        Mascota mascota = new Mascota();

        mascota.setUsuario(usuario);
        mascota.setNombre(formulario.getNombre().trim());

        mascota.setRaza(
                formulario.getRaza() == null
                        || formulario.getRaza().isBlank()
                                ? null
                                : formulario.getRaza().trim());

        mascota.setTamano(formulario.getTamano());

        mascota.setObservaciones(
                formulario.getObservaciones() == null
                        || formulario.getObservaciones().isBlank()
                                ? null
                                : formulario.getObservaciones().trim());

        mascota.setFotoNombre(fotoNombre);

        try {

            mascotaRepository.save(mascota);

        } catch (RuntimeException e) {

            if (fotoNombre != null) {
                try {
                    mascotaFotoService.eliminar(fotoNombre);
                } catch (RuntimeException errorLimpieza) {
                    e.addSuppressed(errorLimpieza);
                }
            }

            throw e;
        }
    }

    @Transactional(readOnly = true)
    public long contarMisMascotas(String correo) {

        Usuario usuario = obtenerUsuario(correo);

        return mascotaRepository.countByUsuario_Id(usuario.getId());
    }

    @Transactional(readOnly = true)
    public String obtenerFotoNombre(Long mascotaId, String correo) {

        Usuario usuario = obtenerUsuario(correo);

        Mascota mascota = mascotaRepository
                .findByIdAndUsuario_Id(mascotaId, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la mascota solicitada."));

        if (mascota.getFotoNombre() == null
                || mascota.getFotoNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "Esta mascota todavía no tiene fotografía.");
        }

        return mascota.getFotoNombre();
    }

    private Usuario obtenerUsuario(String correo) {

        return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el usuario autenticado."));
    }
}