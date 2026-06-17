package com.example.ej_inventado.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.clases.Tipo;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findAll();

    List<Actividad> findByCiudad(String ciudad);
    List<Actividad> findByTipo(Tipo tipo);
    List<Actividad> findByCiudadAndTipo(String ciudad, Tipo tipo);

    List<Actividad> findByCreadorEmail(String email);
    List<Actividad> findByCreadorEmailIsNull();

    // Búsqueda flexible: ciudad, nombre o descripción (case-insensitive)
    @Query("SELECT a FROM Actividad a WHERE " +
           "(:q IS NULL OR :q = '' OR " +
           " LOWER(a.ciudad) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           " LOWER(a.nombre) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           " LOWER(a.descripcion) LIKE LOWER(CONCAT('%',:q,'%')))" +
           " AND (:tipo IS NULL OR a.tipo = :tipo)" +
           " AND (a.precio >= :precioMin)" +
           " AND (a.precio <= :precioMax)")
    List<Actividad> buscar(@Param("q") String q,
                           @Param("tipo") Tipo tipo,
                           @Param("precioMin") int precioMin,
                           @Param("precioMax") int precioMax);
}
