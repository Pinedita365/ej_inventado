package com.example.ej_inventado.Controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.clases.Usuario;
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.EmailService;
import com.example.ej_inventado.utils.PdfGenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controllers {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public Controllers(ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        session.removeAttribute("errorMensaje");
        return "index";
    }

    @GetMapping("/configurar-viaje")
    public String configurarViaje(HttpSession session) {
        if (session.getAttribute("nombre") == null) {
            session.setAttribute("nombre", "Invitado");
        }
        return "datos-viaje";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password) {

        String passwordEncriptada = passwordEncoder.encode(password);

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncriptada);
        nuevo.setRol("ROLE_USER");

        usuarioRepository.save(nuevo);

        return "redirect:/?success=true";
    }

    @GetMapping("/comprobar")
public String valida(
        @RequestParam(name = "nombre") String nombre,
        @RequestParam(name = "ciudad") String ciudad,
        @RequestParam(name = "fEntrada") String fEntradaStr,
        @RequestParam(name = "fSalida") String fSalidaStr,
        HttpSession session, HttpServletResponse response) {

    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fEntrada = LocalDate.parse(fEntradaStr, formatter);
        LocalDate fSalida = LocalDate.parse(fSalidaStr, formatter);
        LocalDate hoy = LocalDate.now();

        if (fEntrada.isBefore(hoy)) {
            session.setAttribute("errorMensaje", "La fecha de inicio no puede ser anterior a hoy.");
            return "redirect:/configurar-viaje";
        }

        if (fSalida.isBefore(fEntrada)) {
            session.setAttribute("errorMensaje", "La fecha de salida no puede ser anterior.");
            return "redirect:/configurar-viaje";
        }

        long diasViaje = java.time.temporal.ChronoUnit.DAYS.between(fEntrada, fSalida) + 1;
        if (diasViaje > 30) {
            session.setAttribute("errorMensaje", "El viaje no puede superar los 30 días.");
            return "redirect:/configurar-viaje";
        }

        String ciudadLimpia = ciudad.trim();
        List<Actividad> actividadesEnCiudad = actividadRepository.findByCiudad(ciudadLimpia);

        if (actividadesEnCiudad == null || actividadesEnCiudad.isEmpty()) {
            session.setAttribute("errorMensaje", "Lo sentimos, no tenemos actividades disponibles en " + ciudadLimpia);
            return "redirect:/configurar-viaje";
        }
        session.setAttribute("nombre", nombre);
        session.setAttribute("ciudad", ciudadLimpia);
        session.setAttribute("fEntrada", fEntradaStr);
        session.setAttribute("fSalida", fSalidaStr);
        session.setAttribute("diasViaje", diasViaje);
        session.setAttribute("cont", 0);
        session.setAttribute("precioCarro", 0);
        session.setAttribute("carrito", new ArrayList<Actividad>());

        return "redirect:/inicio";

    } catch (Exception e) {
        session.setAttribute("errorMensaje", "Datos inválidos o formato de fecha incorrecto.");
        return "redirect:/configurar-viaje";
    }
}

    @GetMapping("/inicio")
    public String inicio(
            @RequestParam(name = "añadirCarrito", required = false) Long idAñadir,
            @RequestParam(name = "orden", required = false) String orden,
            HttpSession session, Model model, Authentication auth) {

        if (auth != null && auth.isAuthenticated()) {
            String nombreReal = "Invitado";
            String emailActual = "";

            if (auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
                Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

                emailActual = (String) attributes.get("email");

                if (attributes.get("name") != null) {
                    nombreReal = attributes.get("name").toString();
                } else if (attributes.get("login") != null) {
                    nombreReal = attributes.get("login").toString();
                }
            } else {
                emailActual = auth.getName();
                Optional<Usuario> optUser = usuarioRepository.findByEmail(emailActual);
                if (optUser.isPresent()) {
                    nombreReal = optUser.get().getNombre();
                } else {
                    nombreReal = emailActual;
                }
            }
            if (idAñadir != null) {
                List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");
                Long diasViaje = (Long) session.getAttribute("diasViaje"); 

                if (carrito == null) {
                    carrito = new ArrayList<Actividad>();
                }
                if (carrito.size() >= diasViaje) {
                    return "redirect:/inicio?errorCarrito=full";
                }

                Optional<Actividad> optAct = actividadRepository.findById(idAñadir);
                if (optAct.isPresent()) {
                    Actividad a = optAct.get();
                    if (!carrito.contains(a)) {
                        carrito.add(a);
                        int cont = (int) session.getAttribute("cont");
                        int precio = (int) session.getAttribute("precioCarro");
                        session.setAttribute("cont", cont + 1);
                        session.setAttribute("precioCarro", precio + a.getPrecio());
                    }
                }
                session.setAttribute("carrito", carrito);
            }
            Optional<Usuario> optUsuarioRol = usuarioRepository.findByEmail(emailActual);
            if (optUsuarioRol.isPresent()) {
                Usuario u = optUsuarioRol.get();
                session.setAttribute("rolUsuario", u.getRol());
            }

            session.setAttribute("nombre", nombreReal);

            System.out.println(">>> USUARIO: " + emailActual);
            System.out.println(">>> ROL EN BD: " + session.getAttribute("rolUsuario"));
        }

        String ciudad = (String) session.getAttribute("ciudad");
        List<Actividad> listaAct = actividadRepository.findByCiudad(ciudad);

        if (idAñadir != null) {
            List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<Actividad>();
            }

            Optional<Actividad> optAct = actividadRepository.findById(idAñadir);
            if (optAct.isPresent()) {
                Actividad a = optAct.get();
                if (!carrito.contains(a)) {
                    carrito.add(a);
                    int cont = (int) session.getAttribute("cont");
                    int precio = (int) session.getAttribute("precioCarro");
                    session.setAttribute("cont", cont + 1);
                    session.setAttribute("precioCarro", precio + a.getPrecio());
                }
            }
            session.setAttribute("carrito", carrito);
        }
        // Ordenación
        if ("precio".equals(orden)) {
            Collections.sort(listaAct);
            session.setAttribute("orden", "precio");
        } else {
            session.setAttribute("orden", "defecto");
        }

        model.addAttribute("listaAct", listaAct);
        return "inicio";
    }

    @GetMapping("/verMas")
    public String verMas(@RequestParam(name = "id") Long id, HttpSession session) {
        Optional<Actividad> opt = actividadRepository.findById(id);
        if (opt.isPresent()) {
            session.setAttribute("act", opt.get());
        }
        return "verMas";
    }

    @GetMapping("/eliminarCarrito")
    public String eliminarCarrito(@RequestParam(name = "idEliminar") Long id, HttpSession session) {
        List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");
        if (carrito != null) {
            Actividad encontrada = null;
            for (Actividad a : carrito) {
                if (a.getId().equals(id)) {
                    encontrada = a;
                    break;
                }
            }
            if (encontrada != null) {
                carrito.remove(encontrada);
                int cont = (int) session.getAttribute("cont");
                int precio = (int) session.getAttribute("precioCarro");
                session.setAttribute("cont", cont - 1);
                session.setAttribute("precioCarro", precio - encontrada.getPrecio());
            }
        }
        return "redirect:/inicio";
    }

    @GetMapping("/confirmarCarrito")
    public String confirmarCarrito() {
        return "confirmarCarrito";
    }
    @GetMapping("/invitado")
    public String paginaInvitado(Model model) {
        model.addAttribute("listaAct", actividadRepository.findAll());
        return "invitado";
    }

    @GetMapping("/planificar")
    public String planificar(
            @RequestParam(name = "actSel", required = false) Integer actSel,
            @RequestParam(name = "posSel", required = false) Integer posSel,
            HttpSession session, Model model) {

        String fEntradaStr = (String) session.getAttribute("fEntrada");
        String fSalidaStr = (String) session.getAttribute("fSalida");

        long dias = ChronoUnit.DAYS.between(LocalDate.parse(fEntradaStr), LocalDate.parse(fSalidaStr)) + 1;
        int diasTotales = (int) dias;

        Actividad[] plan = (Actividad[]) session.getAttribute("plan");
        if (plan == null || plan.length != diasTotales) {
            plan = new Actividad[diasTotales];
        }

        List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");

        if (actSel != null) {
            session.setAttribute("tempAct", carrito.get(actSel));
        }
        if (posSel != null && session.getAttribute("tempAct") != null) {
            plan[posSel] = (Actividad) session.getAttribute("tempAct");
            session.removeAttribute("tempAct");
        }

        session.setAttribute("plan", plan);
        model.addAttribute("plan", plan);
        return "planificar";
    }

    @GetMapping("/final")
    public String finalViaje() {
        return "final";
    }

    @GetMapping("/enviar-itinerario")
public String enviarFinal(HttpSession session, Authentication auth) {
    try {
        Actividad[] plan = (Actividad[]) session.getAttribute("plan");
        String nombre = (String) session.getAttribute("nombre");
        String emailDestino = "";

        // --- INICIO DEL CAMBIO ---
        if (auth instanceof OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            
            // Verificamos si existe el atributo "email" (Google siempre lo da, GitHub no siempre)
            if (attributes.get("email") != null) {
                emailDestino = attributes.get("email").toString();
            } 
            // Si el email es nulo (caso común en GitHub), usamos el login como fallback
            else if (attributes.get("login") != null) {
                emailDestino = attributes.get("login").toString() + "@github.com";
                System.out.println("DEBUG: Email no encontrado en GitHub, usando fallback: " + emailDestino);
            }
        } else {
            // Login manual (email es el username)
            emailDestino = auth.getName();
        }
        // --- FIN DEL CAMBIO ---

        if (emailDestino == null || emailDestino.isEmpty()) {
            throw new Exception("No se pudo determinar el email de destino.");
        }

        byte[] pdfContent = PdfGenerator.generarItinerarioPdf(plan, nombre);
        emailService.enviarItinerarioConPdf(emailDestino, nombre, pdfContent);

        return "redirect:/final?enviado=true";
    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/final?error=true";
    }
}
}