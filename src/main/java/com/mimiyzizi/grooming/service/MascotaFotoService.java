package com.mimiyzizi.grooming.service;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

@Service
public class MascotaFotoService {

    private static final Logger LOGGER = Logger.getLogger(MascotaFotoService.class.getName());

    private static final long TAMANO_MAXIMO = 5L * 1024 * 1024;

    private static final long PIXELES_MAXIMOS = 25_000_000L;

    private final Path directorio;

    public MascotaFotoService(
            @Value("${app.uploads.mascotas-dir}") String directorio) {

        this.directorio = Path.of(directorio)
                .toAbsolutePath()
                .normalize();
    }

    @PostConstruct
    public void inicializar() {
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo inicializar el almacenamiento de fotografías.",
                    e);
        }
    }

    public String guardar(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "La fotografía no puede superar los 5 MB.");
        }

        String extension = validarImagen(archivo);

        String nombreArchivo = UUID.randomUUID() + "." + extension;

        Path destino = directorio.resolve(nombreArchivo)
                .normalize();

        if (!destino.startsWith(directorio)) {
            throw new IllegalArgumentException(
                    "La ruta de almacenamiento no es válida.");
        }

        try (InputStream input = archivo.getInputStream()) {

            Files.copy(input, destino);

            return nombreArchivo;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo guardar la fotografía.",
                    e);
        }
    }

    private String validarImagen(MultipartFile archivo) {

        try (
                InputStream input = archivo.getInputStream();
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new IllegalArgumentException(
                        "El archivo seleccionado no es una imagen válida.");
            }

            Iterator<ImageReader> lectores = ImageIO.getImageReaders(imageInput);

            if (!lectores.hasNext()) {
                throw new IllegalArgumentException(
                        "El archivo seleccionado no es una imagen válida.");
            }

            ImageReader lector = lectores.next();

            try {

                lector.setInput(imageInput, true, true);

                String formato = lector.getFormatName()
                        .toLowerCase(Locale.ROOT);

                String extension;

                if ("jpeg".equals(formato) || "jpg".equals(formato)) {
                    extension = "jpg";
                } else if ("png".equals(formato)) {
                    extension = "png";
                } else {
                    throw new IllegalArgumentException(
                            "Solamente se permiten imágenes JPG y PNG.");
                }

                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);

                long pixeles = (long) ancho * alto;

                if (ancho <= 0 || alto <= 0
                        || pixeles > PIXELES_MAXIMOS
                        || ancho > 8000 || alto > 8000) {

                    throw new IllegalArgumentException(
                            "La resolución de la fotografía es demasiado grande.");
                }

                BufferedImage imagen = lector.read(0);

                if (imagen == null) {
                    throw new IllegalArgumentException(
                            "No se pudo procesar la fotografía.");
                }

                return extension;

            } finally {
                lector.dispose();
            }

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "No se pudo leer la fotografía seleccionada.",
                    e);
        }
    }

    public Path obtenerRuta(String nombreArchivo) {

        if (nombreArchivo == null
                || !nombreArchivo.matches(
                        "[a-f0-9\\-]{36}\\.(jpg|png)")) {

            throw new IllegalArgumentException(
                    "El identificador de la fotografía no es válido.");
        }

        Path archivo = directorio.resolve(nombreArchivo)
                .normalize();

        if (!archivo.startsWith(directorio)
                || !Files.isRegularFile(archivo)) {

            throw new IllegalArgumentException(
                    "La fotografía no está disponible.");
        }

        return archivo;
    }

    public void eliminar(String nombreArchivo) {

        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(obtenerRuta(nombreArchivo));
        } catch (IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "No se pudo eliminar la fotografía: " + nombreArchivo,
                    e);
        }
    }
}