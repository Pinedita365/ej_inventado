package com.example.ej_inventado.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.clases.Tipo;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findAll();

    // Buscar por ciudad
    List<Actividad> findByCiudad(String ciudad);

    // Buscar por tipo
    List<Actividad> findByTipo(Tipo tipo);

    // Buscar por ciudad y tipo
    List<Actividad> findByCiudadAndTipo(String ciudad, Tipo tipo);
}
