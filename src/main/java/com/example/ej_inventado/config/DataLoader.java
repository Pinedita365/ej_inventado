package com.example.ej_inventado.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ej_inventado.clases.Deportiva;
import com.example.ej_inventado.clases.Gastronomica;
import com.example.ej_inventado.clases.Nivel;
import com.example.ej_inventado.clases.Tipo;
import com.example.ej_inventado.clases.Turistica;
import com.example.ej_inventado.repositories.ActividadRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final ActividadRepository actividadRepository;

    public DataLoader(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Si ya hay datos, no cargamos de nuevo
        if (actividadRepository.count() > 0) return;

        // ---- ACTIVIDADES TURÍSTICAS ----
        actividadRepository.saveAll(List.of(
            new Turistica(Tipo.TURISTICA, 120, 25, "Sevilla", "Acuario de Sevilla",
                    "Visita el acuario de Sevilla donde podrás encontrar el tanque de agua de 9m, el más grande de Europa.",
                    "img/acuario.jpg", false),
            new Turistica(Tipo.TURISTICA, 300, 50, "Sevilla", "Visita guiada en Sevilla",
                    "Visita Sevilla con un profesional en la historia de esta ciudad. Visita monumentos como la Giralda, la Torre del Oro, la Plaza de España",
                    "img/giralda.jpg", false),
            new Turistica(Tipo.TURISTICA, 180, 15, "Cantabria", "Reserva Natural de Cabárcenos",
                    "Visita la reserva natural más grande de Europa. Un lugar donde podrás disfrutar de la presencia de animales como leones, elefantes, tigres...",
                    "img/reservanat.jpg", true),
            new Turistica(Tipo.TURISTICA, 120, 0, "Almeria", "Faro de Cabo de Gata",
                    "Visita el faro de Cabo de Gata. Un lugar donde tendrás las mejores vistas de la costa de Almeria.",
                    "img/faroCaboGata.jpg", true)
        ));

        // ---- ACTIVIDADES DEPORTIVAS ----
        actividadRepository.saveAll(List.of(
            new Deportiva(Tipo.DEPORTIVA, 180, 45, "Sevilla", "Kayak por el Guadalquivir",
                    "Esta actividad es perfecta para una tarde calurosa en Sevilla, ideal para aprender un deporte nuevo.",
                    "img/kayak.jpg", Nivel.Bajo),
            new Deportiva(Tipo.DEPORTIVA, 120, 30, "Sevilla", "Visita el Sanchez Pizjuan",
                    "Visita el Sanchez Pizjuan desde la vista de tus jugadores favoritos. Entra a los vestuarios y pisa el campo donde juegan.",
                    "img/sevilla.jpg", Nivel.Bajo),
            new Deportiva(Tipo.DEPORTIVA, 300, 35, "Cantabria", "Descenso del Seya",
                    "Realiza el descenso del Seya con profesionales. Mientras haces deporte, disfrutarás de la naturaleza cantábrica.",
                    "img/descensoSeya.jpg", Nivel.Medio),
            new Deportiva(Tipo.DEPORTIVA, 120, 25, "Almeria", "Snorquel en Cabo de Gata",
                    "Practica snorquel en una de las playas vírgenes de Cabo de Gata y descubre la diversidad marina.",
                    "img/snorkel.jpg", Nivel.Medio),
            new Deportiva(Tipo.DEPORTIVA, 120, 69, "Almeria", "Paseo en lancha",
                    "Disfruta de una lancha durante 2 horas en el Cabo de Gata. Podrás ver focas, tortugas y peces. Requiere carnet de lancha.",
                    "img/lancha.jpg", Nivel.Alto)
        ));

        // ---- ACTIVIDADES GASTRONÓMICAS ----
        actividadRepository.saveAll(List.of(
            new Gastronomica(Tipo.GASTRONOMICA, 180, 40, "Sevilla", "Ruta Cruzcampo",
                    "Disfruta de 3 horas de pura cruzcampo y tapas.", "img/cerveza.jpg", "Formal"),
            new Gastronomica(Tipo.GASTRONOMICA, 120, 60, "Cantabria", "Anchoa",
                    "Disfruta de la mejor cena de anchoas del cantábrico.", "img/anchoas.jpg", "Gala"),
            new Gastronomica(Tipo.GASTRONOMICA, 240, 100, "Cantabria", "Vinos Cantábricos",
                    "Disfruta de la mejor ruta de vinos de la costa cantábrica.", "img/vino.jpg", "Usual"),
            new Gastronomica(Tipo.GASTRONOMICA, 180, 40, "Almeria", "Comida almeriense",
                    "Disfruta de la gastronomía almeriense acompañado de un grupo de personas con los mismos intereses.",
                    "img/comidaalmeria.jpg", "Casual")
        ));

        System.out.println("✅ Actividades iniciales cargadas en BD");
    }
}
