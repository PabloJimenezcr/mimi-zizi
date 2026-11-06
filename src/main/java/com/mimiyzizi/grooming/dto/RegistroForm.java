package com.mimiyzizi.grooming.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre completo.")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.")
    @Pattern(regexp = "^[\\p{L}\\s'.-]+$", message = "Utiliza letras y espacios para tu nombre.")
    private String nombreCompleto;

    @NotBlank(message = "Escribe tu número de teléfono.")
    @Pattern(regexp = "^(?=(?:\\D*\\d){8,15}\\D*$)[0-9+()\\s.-]{8,25}$", message = "Ingresa un teléfono válido.")
    private String telefono;

    @NotBlank(message = "Escribe tu correo electrónico.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 160, message = "El correo es demasiado largo.")
    private String correo;

    @NotBlank(message = "Crea una contraseña.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String contrasena;

    @NotBlank(message = "Confirma tu contraseña.")
    private String confirmarContrasena;

    @AssertTrue(message = "Debes aceptar los términos y la política de privacidad.")
    private boolean aceptaTerminos;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getConfirmarContrasena() { return confirmarContrasena; }
    public void setConfirmarContrasena(String confirmarContrasena) { this.confirmarContrasena = confirmarContrasena; }
    public boolean isAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
}
