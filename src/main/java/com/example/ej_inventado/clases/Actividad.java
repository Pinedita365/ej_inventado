package com.example.ej_inventado.clases;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Actividad implements Descripciones, Comparable<Actividad> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private int duracion;
    private int precio;
    private String ciudad;
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    private String img;

    // Constructor vacío OBLIGATORIO para JPA
    public Actividad() {
    }

    public Actividad(Tipo tipo, int duracion, int precio, String ciudad,
            String nombre, String descripcion, String img) {
        this.tipo = tipo;
        this.duracion = duracion;
        this.precio = precio;
        this.ciudad = ciudad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.img = img;
    }

    // getters y setters
    public Long getId() {
        return id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getDuracion() {
        return duracion;
    }

    public int getPrecio() {
        return precio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImg() {
        return img;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setImg(String img) {
        this.img = img;
    }

    // Ordenar por precio (desc)
    @Override
    public int compareTo(Actividad o) {
        return o.precio - this.precio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Actividad))
            return false;
        Actividad that = (Actividad) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    @Transient
    public int getTiempo() {
        return duracion / 60;
    }

    @Override
    @Transient
    public String getBreveDesc() {
        return descripcion.length() > 50
                ? descripcion.substring(0, 50) + "..."
                : descripcion;
    }
}
