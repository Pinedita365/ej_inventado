package com.example.ej_inventado.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.repositories.ActividadRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/viaje")
@Tag(name = "Viajes API", description = "Endpoints para la búsqueda de actividades y planificación")
public class ViajeRestController {

    private final ActividadRepository actividadRepository;

    public ViajeRestController(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar por ciudad", description = "Devuelve las actividades disponibles en una ciudad específica")
    public List<Actividad> buscarPorCiudad(
            @Parameter(description = "Nombre de la ciudad") @RequestParam String ciudad) {
        return actividadRepository.findByCiudad(ciudad);
    }

    @GetMapping("/detalle/{id}")
    @Operation(summary = "Obtener detalle", description = "Obtiene la información completa de una actividad por su ID")
    public Actividad obtenerDetalle(@PathVariable Long id) {
        return actividadRepository.findById(id).orElse(null);
    }
}