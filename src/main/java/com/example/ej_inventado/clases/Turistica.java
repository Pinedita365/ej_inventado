package com.example.ej_inventado.clases;

import jakarta.persistence.Entity;

@Entity
public class Turistica extends Actividad {

    private boolean vehiculo;

    public Turistica() {}

    public Turistica(Tipo tipo, int duracion, int precio, String ciudad,
                     String nombre, String descripcion, String img, boolean vehiculo) {
        super(tipo, duracion, precio, ciudad, nombre, descripcion, img);
        this.vehiculo = vehiculo;
    }

    public boolean isVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(boolean vehiculo) {
        this.vehiculo = vehiculo;
    }

    @Override
    public String getCategoria() {
        return "Actividad turistica";
    }
}
