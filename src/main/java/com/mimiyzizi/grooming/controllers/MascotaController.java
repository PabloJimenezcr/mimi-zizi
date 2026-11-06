package com.mimiyzizi.grooming.controllers;

import com.mimiyzizi.grooming.domain.TamanoMascota;
import com.mimiyzizi.grooming.dto.MascotaEdicionDTO;
import com.mimiyzizi.grooming.dto.MascotaForm;
import com.mimiyzizi.grooming.service.MascotaFotoService;
import com.mimiyzizi.grooming.service.MascotaService;

import jakarta.validation.Valid;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;

@Controller
@RequestMapping("/cliente/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final MascotaFotoService mascotaFotoService;

    public MascotaController(
            MascotaService mascotaService,
            MascotaFotoService mascotaFotoService) {

        this.mascotaService = mascotaService;
        this.mascotaFotoService = mascotaFotoService;
    }

    @ModelAttribute("tamanos")
    public TamanoMascota[] obtenerTamanos() {
        return TamanoMascota.values();
    }

    // LISTAR MASCOTAS
    @GetMapping
    public String listarMascotas(
            Authentication authentication,
            Model model) {

        String correo = authentication.getName();

        model.addAttribute(
                "mascotas",
                mascotaService.listarMisMascotas(correo));

        model.addAttribute(
                "totalMascotas",
                mascotaService.contarMisMascotas(correo));

        model.addAttribute("paginaCliente", "mascotas");

        return "cliente/mascotas/listado";
    }

    // MOSTRAR FORMULARIO DE REGISTRO
    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {

        model.addAttribute("mascotaForm", new MascotaForm());
        model.addAttribute("modoEdicion", false);
        model.addAttribute("tieneFoto", false);
        model.addAttribute("paginaCliente", "mascotas");

        return "cliente/mascotas/nueva";
    }

    // REGISTRAR MASCOTA
    @PostMapping
    public String registrarMascota(
            @Valid @ModelAttribute("mascotaForm") MascotaForm formulario,
            BindingResult bindingResult,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("modoEdicion", false);
            model.addAttribute("tieneFoto", false);
            model.addAttribute("paginaCliente", "mascotas");

            return "cliente/mascotas/nueva";
        }

        try {

            mascotaService.registrarMascota(
                    authentication.getName(),
                    formulario,
                    foto);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorFoto", e.getMessage());
            model.addAttribute("modoEdicion", false);
            model.addAttribute("tieneFoto", false);
            model.addAttribute("paginaCliente", "mascotas");

            return "cliente/mascotas/nueva";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "¡La mascota se ha registrado correctamente!");

        return "redirect:/cliente/mascotas";
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarEdicion(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        MascotaEdicionDTO datos = obtenerDatosPropios(
                id,
                authentication.getName());

        model.addAttribute("mascotaForm", datos.formulario());
        model.addAttribute("tieneFoto", datos.tieneFoto());
        model.addAttribute("mascotaId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("paginaCliente", "mascotas");

        return "cliente/mascotas/nueva";
    }

    // ACTUALIZAR MASCOTA
    @PostMapping("/{id}")
    public String actualizarMascota(
            @PathVariable Long id,
            @Valid @ModelAttribute("mascotaForm") MascotaForm formulario,
            BindingResult bindingResult,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        MascotaEdicionDTO datos = obtenerDatosPropios(
                id,
                authentication.getName());

        if (bindingResult.hasErrors()) {

            cargarContextoEdicion(model, id, datos);

            return "cliente/mascotas/nueva";
        }

        try {

            mascotaService.actualizarMascota(
                    authentication.getName(),
                    id,
                    formulario,
                    foto);

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorFoto", e.getMessage());

            cargarContextoEdicion(model, id, datos);

            return "cliente/mascotas/nueva";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La información de tu mascota se actualizó correctamente.");

        return "redirect:/cliente/mascotas";
    }

    // MOSTRAR FOTOGRAFÍA PROTEGIDA
    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> mostrarFoto(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            String nombreArchivo = mascotaService.obtenerFotoNombre(
                    id,
                    authentication.getName());

            Path ruta = mascotaFotoService.obtenerRuta(nombreArchivo);

            Resource recurso = new FileSystemResource(ruta);

            MediaType tipo = nombreArchivo.endsWith(".png")
                    ? MediaType.IMAGE_PNG
                    : MediaType.IMAGE_JPEG;

            return ResponseEntity.ok()
                    .contentType(tipo)
                    .cacheControl(CacheControl.noStore().cachePrivate())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(recurso);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La fotografía no está disponible.");
        }
    }

    // VERIFICAR QUE LA MASCOTA PERTENEZCA AL CLIENTE
    private MascotaEdicionDTO obtenerDatosPropios(
            Long id,
            String correo) {

        try {

            return mascotaService.obtenerDatosEdicion(correo, id);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró la mascota solicitada.");
        }
    }

    // RESTAURAR EL CONTEXTO DEL FORMULARIO
    private void cargarContextoEdicion(
            Model model,
            Long id,
            MascotaEdicionDTO datos) {

        model.addAttribute("tieneFoto", datos.tieneFoto());
        model.addAttribute("mascotaId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("paginaCliente", "mascotas");
    }
}