package com.mimiyzizi.grooming.dto;

import com.mimiyzizi.grooming.domain.TamanoMascota;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MascotaForm {

    @NotBlank(message = "El nombre de la mascota es obligatorio.")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres.")
    private String nombre;

    @Size(max = 80, message = "La raza no puede superar los 80 caracteres.")
    private String raza;

    @NotNull(message = "Debes seleccionar el tamaño de la mascota.")
    private TamanoMascota tamano;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    private String observaciones;

    private boolean eliminarFoto;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public TamanoMascota getTamano() {
        return tamano;
    }

    public void setTamano(TamanoMascota tamano) {
        this.tamano = tamano;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isEliminarFoto() {
        return eliminarFoto;
    }

    public void setEliminarFoto(boolean eliminarFoto) {
        this.eliminarFoto = eliminarFoto;
    }
}