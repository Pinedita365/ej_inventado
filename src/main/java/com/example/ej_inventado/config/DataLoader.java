package com.example.ej_inventado.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ej_inventado.clases.Deportiva;
import com.example.ej_inventado.clases.Gastronomica;
import com.example.ej_inventado.clases.Nivel;
import com.example.ej_inventado.clases.Resena;
import com.example.ej_inventado.clases.Tipo;
import com.example.ej_inventado.clases.Turistica;
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.repositories.ResenaRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final ActividadRepository actividadRepository;
    private final ResenaRepository resenaRepository;

    public DataLoader(ActividadRepository actividadRepository,
                      ResenaRepository resenaRepository) {
        this.actividadRepository = actividadRepository;
        this.resenaRepository    = resenaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (actividadRepository.count() > 0) return;

        // ── TURÍSTICAS ────────────────────────────────────────────────────────────
        Turistica acuario = new Turistica(Tipo.TURISTICA, 120, 25, "Sevilla",
                "Acuario de Sevilla",
                "Visita el acuario de Sevilla donde podrás encontrar el tanque de agua de 9m, el más grande de Europa.",
                "img/acuario.jpg", false);
        acuario.setLatitud(37.3927); acuario.setLongitud(-5.9965);

        Turistica giralda = new Turistica(Tipo.TURISTICA, 300, 50, "Sevilla",
                "Visita guiada en Sevilla",
                "Visita Sevilla con un profesional en la historia de esta ciudad. Monumentos como la Giralda, la Torre del Oro, la Plaza de España.",
                "img/giralda.jpg", false);
        giralda.setLatitud(37.3861); giralda.setLongitud(-5.9925);

        Turistica cabarceno = new Turistica(Tipo.TURISTICA, 180, 15, "Cantabria",
                "Reserva Natural de Cabárcenos",
                "Visita la reserva natural más grande de Europa. Disfruta de animales como leones, elefantes y tigres.",
                "img/reservanat.jpg", true);
        cabarceno.setLatitud(43.3588); cabarceno.setLongitud(-3.8419);

        Turistica faro = new Turistica(Tipo.TURISTICA, 120, 0, "Almeria",
                "Faro de Cabo de Gata",
                "Visita el faro de Cabo de Gata. Un lugar donde tendrás las mejores vistas de la costa de Almería.",
                "img/faroCaboGata.jpg", true);
        faro.setLatitud(36.7299); faro.setLongitud(-2.1897);

        actividadRepository.saveAll(List.of(acuario, giralda, cabarceno, faro));

        // ── DEPORTIVAS ────────────────────────────────────────────────────────────
        Deportiva kayak = new Deportiva(Tipo.DEPORTIVA, 180, 45, "Sevilla",
                "Kayak por el Guadalquivir",
                "Actividad perfecta para una tarde calurosa en Sevilla, ideal para aprender un deporte nuevo sobre el río.",
                "img/kayak.jpg", Nivel.Bajo);
        kayak.setLatitud(37.3895); kayak.setLongitud(-5.9960);

        Deportiva estadio = new Deportiva(Tipo.DEPORTIVA, 120, 30, "Sevilla",
                "Visita el Sánchez Pizjuán",
                "Visita el estadio desde la vista de tus jugadores favoritos. Entra a los vestuarios y pisa el campo.",
                "img/sevilla.jpg", Nivel.Bajo);
        estadio.setLatitud(37.3843); estadio.setLongitud(-5.9705);

        Deportiva seya = new Deportiva(Tipo.DEPORTIVA, 300, 35, "Cantabria",
                "Descenso del Sella",
                "Realiza el descenso del Sella con profesionales. Mientras haces deporte, disfrutarás de la naturaleza cantábrica.",
                "img/descensoSeya.jpg", Nivel.Medio);
        seya.setLatitud(43.3680); seya.setLongitud(-5.0592);

        Deportiva snorkel = new Deportiva(Tipo.DEPORTIVA, 120, 25, "Almeria",
                "Snórquel en Cabo de Gata",
                "Practica snórquel en una de las playas vírgenes de Cabo de Gata y descubre la diversidad marina.",
                "img/snorkel.jpg", Nivel.Medio);
        snorkel.setLatitud(36.7338); snorkel.setLongitud(-2.1921);

        Deportiva lancha = new Deportiva(Tipo.DEPORTIVA, 120, 69, "Almeria",
                "Paseo en lancha",
                "Disfruta de una lancha durante 2 horas en el Cabo de Gata. Podrás ver focas, tortugas y peces.",
                "img/lancha.jpg", Nivel.Alto);
        lancha.setLatitud(36.7450); lancha.setLongitud(-2.2100);

        actividadRepository.saveAll(List.of(kayak, estadio, seya, snorkel, lancha));

        // ── GASTRONÓMICAS ─────────────────────────────────────────────────────────
        Gastronomica cruzcampo = new Gastronomica(Tipo.GASTRONOMICA, 180, 40, "Sevilla",
                "Ruta Cruzcampo",
                "Disfruta de 3 horas de pura Cruzcampo y tapas sevillanas en los mejores bares del centro.",
                "img/cerveza.jpg", "Formal");
        cruzcampo.setLatitud(37.3886); cruzcampo.setLongitud(-5.9823);

        Gastronomica anchoa = new Gastronomica(Tipo.GASTRONOMICA, 120, 60, "Cantabria",
                "Cena de Anchoas del Cantábrico",
                "Disfruta de la mejor cena de anchoas del Cantábrico en un restaurante con vistas al mar.",
                "img/anchoas.jpg", "Gala");
        anchoa.setLatitud(43.4623); anchoa.setLongitud(-3.8099);

        Gastronomica vinos = new Gastronomica(Tipo.GASTRONOMICA, 240, 100, "Cantabria",
                "Ruta de Vinos Cantábricos",
                "Disfruta de la mejor ruta de vinos de la costa cantábrica con maridaje incluido.",
                "img/vino.jpg", "Usual");
        vinos.setLatitud(43.4580); vinos.setLongitud(-3.8150);

        Gastronomica almeriense = new Gastronomica(Tipo.GASTRONOMICA, 180, 40, "Almeria",
                "Gastronomía Almeriense",
                "Disfruta de la gastronomía almeriense acompañado de un grupo de personas con los mismos intereses.",
                "img/comidaalmeria.jpg", "Casual");
        almeriense.setLatitud(36.8381); almeriense.setLongitud(-2.4597);

        actividadRepository.saveAll(List.of(cruzcampo, anchoa, vinos, almeriense));

        // ── RESEÑAS DE SEMILLA ────────────────────────────────────────────────────
        if (resenaRepository.count() == 0) {
            resenaRepository.saveAll(List.of(
                resena("María G.",    5, "Planifiqué toda mi escapada a Sevilla en menos de 10 minutos. Las actividades son muy variadas y el PDF del itinerario me encantó.", "Sevilla",   -6),
                resena("Carlos R.",   5, "Nunca había encontrado una herramienta tan fácil de usar para organizar actividades. ¡La recomiendo a todo el mundo!", "Cantabria", -5),
                resena("Ana M.",      5, "Las actividades gastronómicas de Cantabria son espectaculares. Gracias a TriPing descubrí restaurantes que no hubiera encontrado solo.", "Cantabria", -4),
                resena("Luis P.",     5, "El snórquel en Cabo de Gata fue una pasada. TriPing me lo recomendó y fue la mejor decisión de mis vacaciones.", "Almería",   -3),
                resena("Sara T.",     5, "La planificación día a día es perfecta. Saber exactamente qué voy a hacer cada jornada me da mucha tranquilidad.", "Almería",   -2),
                resena("Javier L.",   4, "Muy buena plataforma. Espero que sigan añadiendo más ciudades. Las actividades deportivas son muy variadas y bien descritas.", "Sevilla",   -1)
            ));
        }
    }

    private Resena resena(String autor, int estrellas, String texto, String ciudad, long diasAtras) {
        Resena r = new Resena();
        r.setNombreAutor(autor);
        r.setEstrellas(estrellas);
        r.setTexto(texto);
        r.setCiudad(ciudad);
        r.setFecha(LocalDateTime.now().plusDays(diasAtras));
        r.setAprobada(true);
        return r;
    }
}
