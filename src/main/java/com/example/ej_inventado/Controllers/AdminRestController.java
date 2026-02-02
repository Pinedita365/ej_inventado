package com.example.ej_inventado.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.repositories.ActividadRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Endpoints para la gestión de actividades por parte del administrador")
public class AdminRestController {

    private final ActividadRepository actividadRepository;

    public AdminRestController(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    @GetMapping("/actividades")
    @Operation(summary = "Obtener todas las actividades", description = "Devuelve una lista de todas las actividades registradas")
    public List<Actividad> listarTodas() {
        return actividadRepository.findAll();
    }

    @DeleteMapping("/actividad/{id}")
    @Operation(summary = "Eliminar actividad", description = "Elimina una actividad de la base de datos por su ID")
    public void eliminar(@PathVariable Long id) {
        actividadRepository.deleteById(id);
    }
}