package com.example.ej_inventado.Controllers;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.clases.Deportiva;
import com.example.ej_inventado.clases.Gastronomica;
import com.example.ej_inventado.clases.Nivel;
import com.example.ej_inventado.clases.Tipo;
import com.example.ej_inventado.clases.Turistica;
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.services.CloudinaryService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ActividadRepository actividadRepository;
    private final CloudinaryService cloudinaryService;

    public AdminController(ActividadRepository actividadRepository, CloudinaryService cloudinaryService) {
        this.actividadRepository = actividadRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("")
    public String adminIndex() {
        return "redirect:/admin/panel";
    }

    @GetMapping("/panel")
    public String panelAdmin(Model model) {
        model.addAttribute("actividades", actividadRepository.findAll());
        return "admin/panel"; // Esto busca: src/main/resources/templates/admin/panel.html
    }

    @PostMapping("/nueva-actividad")
    public String nuevaActividad(@ModelAttribute Actividad actividad) {
        actividadRepository.save(actividad);
        return "redirect:/admin/panel?success=true";
    }

    @PostMapping("/guardar")
    public String guardarActividad(
            @RequestParam("nombre") String nombre,
            @RequestParam("ciudad") String ciudad,
            @RequestParam("precio") int precio,
            @RequestParam("duracion") int duracion,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("tipo") String tipo,
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam(value = "nivel", required = false) String nivelStr, // Cambiado a String
            @RequestParam(value = "vestimenta", required = false) String vestimenta,
            @RequestParam(value = "vehiculo", required = false) Boolean vehiculo) throws IOException {

        String urlImagen = cloudinaryService.uploadImage(imagen);
        Actividad nueva;
        Tipo tipoEnum = Tipo.valueOf(tipo.toUpperCase());

        if (tipoEnum == Tipo.DEPORTIVA) {
            // Convertimos el String a Enum solo si es Deportiva
            Nivel nivelEnum = null;
            if (nivelStr != null && !nivelStr.isEmpty()) {
                nivelEnum = Nivel.valueOf(nivelStr); // Aquí usará "Alto", "Bajo" o "Medio"
            }
            nueva = new Deportiva(tipoEnum, duracion, precio, ciudad, nombre, descripcion, urlImagen, nivelEnum);
        } else if (tipoEnum == Tipo.GASTRONOMICA) {
            nueva = new Gastronomica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, urlImagen, vestimenta);
        } else {
            nueva = new Turistica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, urlImagen,
                    vehiculo != null && vehiculo);
        }

        actividadRepository.save(nueva);
        return "redirect:/inicio";
    }
}