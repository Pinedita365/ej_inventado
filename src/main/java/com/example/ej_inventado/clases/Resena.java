package com.example.ej_inventado.clases;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreAutor;

    @Column(nullable = false, length = 600)
    private String texto;

    private int estrellas;

    private String ciudad;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private boolean aprobada = true;

    public Resena() {}

    public Long getId()              { return id; }
    public String getNombreAutor()   { return nombreAutor; }
    public String getTexto()         { return texto; }
    public int getEstrellas()        { return estrellas; }
    public String getCiudad()        { return ciudad; }
    public LocalDateTime getFecha()  { return fecha; }
    public boolean isAprobada()      { return aprobada; }

    public void setNombreAutor(String nombreAutor) { this.nombreAutor = nombreAutor; }
    public void setTexto(String texto)             { this.texto = texto; }
    public void setEstrellas(int estrellas)        { this.estrellas = estrellas; }
    public void setCiudad(String ciudad)           { this.ciudad = ciudad; }
    public void setFecha(LocalDateTime fecha)      { this.fecha = fecha; }
    public void setAprobada(boolean aprobada)      { this.aprobada = aprobada; }

    public String getStarsHtml() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= estrellas ? "★" : "☆");
        }
        return sb.toString();
    }

    public String getIniciales() {
        if (nombreAutor == null || nombreAutor.isBlank()) return "?";
        String[] partes = nombreAutor.trim().split("\\s+");
        if (partes.length >= 2) return (partes[0].charAt(0) + "" + partes[1].charAt(0)).toUpperCase();
        return nombreAutor.substring(0, Math.min(2, nombreAutor.length())).toUpperCase();
    }
}
