package com.example.ej_inventado.clases;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Deportiva extends Actividad {

    @Enumerated(EnumType.STRING)
    private Nivel nivel;

    public Deportiva() {}

    public Deportiva(Tipo tipo, int duracion, int precio, String ciudad,
                     String nombre, String descripcion, String img, Nivel nivel) {
        super(tipo, duracion, precio, ciudad, nombre, descripcion, img);
        this.nivel = nivel;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    @Override
    public String getCategoria() {
        return "Actividad deportiva";
    }
}
