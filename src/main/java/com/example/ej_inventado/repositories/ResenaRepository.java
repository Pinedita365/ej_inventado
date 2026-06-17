package com.example.ej_inventado.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ej_inventado.clases.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findTop12ByAprobadaOrderByFechaDesc(boolean aprobada);

    long countByAprobada(boolean aprobada);
}
