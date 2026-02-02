package com.example.ej_inventado.clases;

import jakarta.persistence.Entity;

@Entity
public class Gastronomica extends Actividad {

    private String vestimenta;

    public Gastronomica() {}

    public Gastronomica(Tipo tipo, int duracion, int precio, String ciudad,
                        String nombre, String descripcion, String img, String vestimenta) {
        super(tipo, duracion, precio, ciudad, nombre, descripcion, img);
        this.vestimenta = vestimenta;
    }

    public String getVestimenta() {
        return vestimenta;
    }

    public void setVestimenta(String vestimenta) {
        this.vestimenta = vestimenta;
    }

    @Override
    public String getCategoria() {
        return "Actividad gastronomica";
    }
}
