package com.example.ej_inventado.Controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ej_inventado.clases.Actividad;
import com.example.ej_inventado.clases.Tipo;
import com.example.ej_inventado.clases.Usuario;
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.CloudinaryService;
import com.example.ej_inventado.services.EmailService;
import com.example.ej_inventado.utils.PdfGenerator;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controllers {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    public Controllers(ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            CloudinaryService cloudinaryService) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.cloudinaryService = cloudinaryService;
    }

    // ── Hub eliminado: redirige al inicio ─────────────────────────────────────
    @GetMapping("/hub")
    public String hub() {
        return "redirect:/";
    }

    // ── Explorar: todas las actividades con búsqueda y filtros ────────────────
    @GetMapping("/explorar")
    public String explorar(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "0")   int precioMin,
            @RequestParam(required = false, defaultValue = "9999") int precioMax,
            HttpSession session, Authentication auth, Model model) {

        resolverUsuario(auth, session);

        Tipo tipoEnum = null;
        if (tipo != null && !tipo.isBlank() && !tipo.equals("TODAS")) {
            try { tipoEnum = Tipo.valueOf(tipo); } catch (IllegalArgumentException ignored) {}
        }

        List<Actividad> lista = actividadRepository.buscar(
                q.isBlank() ? null : q, tipoEnum, precioMin, precioMax);

        model.addAttribute("listaAct",  lista);
        model.addAttribute("q",         q);
        model.addAttribute("tipo",      tipo != null ? tipo : "TODAS");
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("nombre",    session.getAttribute("nombre"));
        return "explorar";
    }

    // ── Configurar viaje ──────────────────────────────────────────────────────
    @GetMapping("/configurar-viaje")
    public String configurarViaje(HttpSession session, Authentication auth) {
        resolverUsuario(auth, session);
        return "datos-viaje";
    }

    // ── Registro de usuario ───────────────────────────────────────────────────
    @PostMapping("/registrar")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "") String confirmPassword) {

        nombre = nombre != null ? nombre.trim() : "";
        email  = email  != null ? email.trim().toLowerCase() : "";

        if (nombre.isEmpty() || nombre.length() > 100)
            return "redirect:/login?regError=datos_invalidos";
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches())
            return "redirect:/login?regError=datos_invalidos";
        if (password == null || password.length() < 8)
            return "redirect:/login?regError=password_debil";
        if (!password.equals(confirmPassword))
            return "redirect:/login?regError=password_no_coincide";
        if (usuarioRepository.findByEmail(email).isPresent())
            return "redirect:/login?regError=email_en_uso";

        try {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setPassword(passwordEncoder.encode(password));
            nuevo.setRol("ROLE_USER");
            nuevo.setProveedor("LOCAL");
            usuarioRepository.save(nuevo);
            return "redirect:/login?success=true";
        } catch (Exception e) {
            return "redirect:/login?regError=error_servidor";
        }
    }

    // ── Validar configuración de viaje ────────────────────────────────────────
    @GetMapping("/comprobar")
    public String valida(
            @RequestParam String nombre,
            @RequestParam String ciudad,
            @RequestParam String fEntrada,
            @RequestParam String fSalida,
            HttpSession session, HttpServletResponse response) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate inicio = LocalDate.parse(fEntrada, fmt);
            LocalDate fin    = LocalDate.parse(fSalida,  fmt);
            LocalDate hoy    = LocalDate.now();

            if (inicio.isBefore(hoy)) {
                session.setAttribute("errorMensaje", "La fecha de inicio no puede ser anterior a hoy.");
                return "redirect:/configurar-viaje";
            }
            if (fin.isBefore(inicio)) {
                session.setAttribute("errorMensaje", "La fecha de salida no puede ser anterior a la de inicio.");
                return "redirect:/configurar-viaje";
            }
            long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
            if (dias > 30) {
                session.setAttribute("errorMensaje", "El viaje no puede superar los 30 días.");
                return "redirect:/configurar-viaje";
            }

            session.setAttribute("nombre",    nombre);
            session.setAttribute("ciudad",    ciudad.trim());
            session.setAttribute("fEntrada",  fEntrada);
            session.setAttribute("fSalida",   fSalida);
            session.setAttribute("diasViaje", dias);
            session.setAttribute("cont",      0);
            session.setAttribute("precioCarro", 0);
            session.setAttribute("carrito",   new ArrayList<Actividad>());

            return "redirect:/inicio";
        } catch (Exception e) {
            session.setAttribute("errorMensaje", "Datos inválidos o formato de fecha incorrecto.");
            return "redirect:/configurar-viaje";
        }
    }

    // ── Inicio: actividades del viaje ─────────────────────────────────────────
    @GetMapping("/inicio")
    public String inicio(
            @RequestParam(name = "añadirCarrito", required = false) Long idAñadir,
            @RequestParam(name = "orden", required = false) String orden,
            HttpSession session, Model model, Authentication auth) {

        resolverUsuario(auth, session);

        if (idAñadir != null) añadirAlCarrito(idAñadir, session);

        String ciudad = (String) session.getAttribute("ciudad");
        if (ciudad == null) return "redirect:/";

        List<Actividad> listaAct = actividadRepository.findByCiudad(ciudad.trim());
        if (listaAct == null || listaAct.isEmpty())
            model.addAttribute("mensajeVacio", "No hay actividades disponibles para " + ciudad);

        if ("precio".equals(orden)) {
            Collections.sort(listaAct);
            session.setAttribute("orden", "precio");
        } else {
            session.setAttribute("orden", "defecto");
        }

        model.addAttribute("listaAct", listaAct);
        model.addAttribute("nombre",   session.getAttribute("nombre"));
        return "inicio";
    }

    // ── Ver más detalle ────────────────────────────────────────────────────────
    @GetMapping("/verMas")
    public String verMas(@RequestParam Long id, HttpSession session) {
        actividadRepository.findById(id).ifPresent(a -> session.setAttribute("act", a));
        return "verMas";
    }

    // ── Eliminar del carrito ───────────────────────────────────────────────────
    @GetMapping("/eliminarCarrito")
    public String eliminarCarrito(@RequestParam Long idEliminar, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");
        if (carrito != null && idEliminar != null) {
            carrito.stream()
                .filter(a -> idEliminar.equals(a.getId()))
                .findFirst()
                .ifPresent(found -> {
                    carrito.remove(found);
                    session.setAttribute("cont", (int) session.getAttribute("cont") - 1);
                    session.setAttribute("precioCarro", (int) session.getAttribute("precioCarro") - found.getPrecio());
                });
        }
        return "redirect:/inicio";
    }

    @GetMapping("/confirmarCarrito")
    public String confirmarCarrito() { return "confirmarCarrito"; }

    // ── Invitado ───────────────────────────────────────────────────────────────
    @GetMapping("/invitado")
    public String paginaInvitado(Model model) {
        model.addAttribute("listaAct", actividadRepository.findAll());
        return "invitado";
    }

    // ── Planificador por días ──────────────────────────────────────────────────
    @GetMapping("/planificar")
    public String planificar(
            @RequestParam(required = false) Integer actSel,
            @RequestParam(required = false) Integer posSel,
            HttpSession session, Model model) {

        String fE = (String) session.getAttribute("fEntrada");
        String fS = (String) session.getAttribute("fSalida");
        if (fE == null || fS == null) return "redirect:/";

        int dias = (int)(ChronoUnit.DAYS.between(LocalDate.parse(fE), LocalDate.parse(fS)) + 1);
        Actividad[] plan = (Actividad[]) session.getAttribute("plan");
        if (plan == null || plan.length != dias) plan = new Actividad[dias];

        @SuppressWarnings("unchecked")
        List<Actividad> carrito = (List<Actividad>) session.getAttribute("carrito");

        if (actSel != null && carrito != null) session.setAttribute("tempAct", carrito.get(actSel));
        if (posSel != null && session.getAttribute("tempAct") != null) {
            plan[posSel] = (Actividad) session.getAttribute("tempAct");
            session.removeAttribute("tempAct");
        }

        session.setAttribute("plan", plan);
        model.addAttribute("plan", plan);
        return "planificar";
    }

    @GetMapping("/final")
    public String finalViaje() { return "final"; }

    @GetMapping("/enviar-itinerario")
    public String enviarFinal(HttpSession session, Authentication auth) {
        try {
            Actividad[] plan  = (Actividad[]) session.getAttribute("plan");
            String nombre     = (String) session.getAttribute("nombre");
            String emailDest  = "";

            if (auth instanceof OAuth2AuthenticationToken token) {
                Map<String, Object> attrs = token.getPrincipal().getAttributes();
                emailDest = attrs.get("email") != null
                        ? attrs.get("email").toString()
                        : attrs.get("login") + "@github.com";
            } else {
                emailDest = auth.getName();
            }

            byte[] pdf = PdfGenerator.generarItinerarioPdf(plan, nombre);
            emailService.enviarItinerarioConPdf(emailDest, nombre, pdf);
            return "redirect:/final?enviado=true";
        } catch (Exception e) {
            return "redirect:/final?error=true";
        }
    }

    // ── Perfil de usuario ────────────────────────────────────────────────────
    @GetMapping("/perfil")
    public String perfil(Authentication auth, HttpSession session, Model model) {
        resolverUsuario(auth, session);
        String email = resolverEmail(auth);
        usuarioRepository.findByEmail(email).ifPresent(u -> model.addAttribute("usuario", u));
        return "perfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@RequestParam String nombre,
                                @RequestParam(required = false) MultipartFile fotoPerfil,
                                Authentication auth, HttpSession session) throws IOException {
        String nombreLimpio = nombre != null ? nombre.trim() : "";
        if (nombreLimpio.isEmpty() || nombreLimpio.length() > 100)
            return "redirect:/perfil?errorNombre=true";

        String email = resolverEmail(auth);
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            if (!nombreLimpio.isBlank()) {
                u.setNombre(nombreLimpio);
            }
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                try {
                    u.setFotoPerfil(cloudinaryService.uploadFotoPerfil(fotoPerfil));
                } catch (IOException ignored) {}
            }
            usuarioRepository.save(u);
            session.setAttribute("nombre",     u.getNombre());
            session.setAttribute("fotoPerfil", u.getFotoPerfil());
        });
        return "redirect:/perfil?success=true";
    }

    // ── Helper: obtener email del usuario autenticado ─────────────────────────
    private String resolverEmail(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken token) {
            Object e = token.getPrincipal().getAttributes().get("email");
            if (e != null) return e.toString();
            Object l = token.getPrincipal().getAttributes().get("login");
            return l != null ? l + "@github.com" : "";
        }
        return auth.getName();
    }

    // ── Helper: resolver nombre y rol del usuario autenticado ─────────────────
    private void resolverUsuario(Authentication auth, HttpSession session) {
        if (auth == null || !auth.isAuthenticated()) return;

        String email = "", nombre = "Invitado";

        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attrs = oauthToken.getPrincipal().getAttributes();
            email  = attrs.get("email") != null ? attrs.get("email").toString() : "";
            nombre = attrs.get("name")  != null ? attrs.get("name").toString()
                   : attrs.get("login") != null ? attrs.get("login").toString() : email;
        } else {
            email  = auth.getName();
            nombre = usuarioRepository.findByEmail(email)
                        .map(Usuario::getNombre).orElse(email);
        }

        session.setAttribute("nombre", nombre);
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            session.setAttribute("rolUsuario",  u.getRol());
            session.setAttribute("fotoPerfil",  u.getFotoPerfil());
        });
    }

    private void añadirAlCarrito(Long id, HttpSession session) {
        if (id == null) return;
        @SuppressWarnings("unchecked")
        List<Actividad> carritoExistente = (List<Actividad>) session.getAttribute("carrito");
        Long dias = (Long) session.getAttribute("diasViaje");
        final List<Actividad> carrito = carritoExistente != null ? carritoExistente : new ArrayList<>();
        if (dias == null || carrito.size() >= dias) return;

        actividadRepository.findById(id).ifPresent(a -> {
            if (!carrito.contains(a)) {
                carrito.add(a);
                session.setAttribute("cont", (int) session.getAttribute("cont") + 1);
                session.setAttribute("precioCarro", (int) session.getAttribute("precioCarro") + a.getPrecio());
            }
        });
        session.setAttribute("carrito", carrito);
    }
}
