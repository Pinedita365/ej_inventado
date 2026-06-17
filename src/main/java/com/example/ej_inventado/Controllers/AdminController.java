package com.example.ej_inventado.Controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.ej_inventado.clases.Usuario;
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.CloudinaryService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ActividadRepository actividadRepository,
                           UsuarioRepository usuarioRepository,
                           CloudinaryService cloudinaryService,
                           PasswordEncoder passwordEncoder) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository   = usuarioRepository;
        this.cloudinaryService   = cloudinaryService;
        this.passwordEncoder     = passwordEncoder;
    }

    @GetMapping({ "", "/" })
    public String adminIndex() {
        return "redirect:/admin/usuarios";
    }

    // Compatibilidad con enlaces antiguos
    @GetMapping("/panel")
    public String panelLegacy() {
        return "redirect:/admin/usuarios";
    }

    private void addStats(Model model) {
        model.addAttribute("totalUsuarios",    usuarioRepository.count());
        model.addAttribute("totalEmpresas",    usuarioRepository.findByRol("ROLE_EMPRESA").size());
        model.addAttribute("totalActividades", actividadRepository.count());
    }

    // ── Página: Usuarios ───────────────────────────────────────────────────────
    @GetMapping("/usuarios")
    public String pageUsuarios(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "TODOS") String rolFiltro,
            Model model) {
        List<Usuario> usuarios;
        if (!q.isBlank()) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);
        } else if (!"TODOS".equals(rolFiltro)) {
            usuarios = usuarioRepository.findByRol(rolFiltro);
        } else {
            usuarios = usuarioRepository.findAll();
        }
        model.addAttribute("usuarios",  usuarios);
        model.addAttribute("q",         q);
        model.addAttribute("rolFiltro", rolFiltro);
        addStats(model);
        return "admin/usuarios";
    }

    // ── Página: Empresas ───────────────────────────────────────────────────────
    @GetMapping("/empresas")
    public String pageEmpresas(Model model) {
        model.addAttribute("empresas", usuarioRepository.findByRol("ROLE_EMPRESA"));
        addStats(model);
        return "admin/empresas";
    }

    // ── Página: Actividades ────────────────────────────────────────────────────
    @GetMapping("/actividades")
    public String pageActividades(Model model) {
        model.addAttribute("actividades", actividadRepository.findAll());
        addStats(model);
        return "admin/actividades";
    }

    private static final java.util.regex.Pattern EMAIL_RE =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ── Crear actividad (admin) ────────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardarActividad(
            @RequestParam String nombre,
            @RequestParam String ciudad,
            @RequestParam int precio,
            @RequestParam int duracion,
            @RequestParam String descripcion,
            @RequestParam String tipo,
            @RequestParam MultipartFile imagen,
            @RequestParam(required = false) String nivelStr,
            @RequestParam(required = false) String vestimenta,
            @RequestParam(required = false) Boolean vehiculo,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud) throws IOException {

        nombre      = nombre      != null ? nombre.trim()      : "";
        ciudad      = ciudad      != null ? ciudad.trim()       : "";
        descripcion = descripcion != null ? descripcion.trim()  : "";

        if (nombre.isEmpty() || nombre.length() > 100)
            return "redirect:/admin/actividades?errorGuardar=nombre";
        if (ciudad.isEmpty() || ciudad.length() > 100)
            return "redirect:/admin/actividades?errorGuardar=ciudad";
        if (precio < 0 || precio > 10000)
            return "redirect:/admin/actividades?errorGuardar=precio";
        if (duracion < 1 || duracion > 1440)
            return "redirect:/admin/actividades?errorGuardar=duracion";
        if (descripcion.isEmpty() || descripcion.length() > 500)
            return "redirect:/admin/actividades?errorGuardar=descripcion";
        if (imagen == null || imagen.isEmpty())
            return "redirect:/admin/actividades?errorGuardar=imagen";

        Tipo tipoEnum;
        try { tipoEnum = Tipo.valueOf(tipo.toUpperCase()); }
        catch (Exception e) { return "redirect:/admin/actividades?errorGuardar=tipo"; }

        String imgUrl = cloudinaryService.uploadImage(imagen);
        Actividad nueva;

        if (tipoEnum == Tipo.DEPORTIVA) {
            Nivel nivel = (nivelStr != null && !nivelStr.isEmpty()) ? Nivel.valueOf(nivelStr) : Nivel.Bajo;
            nueva = new Deportiva(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl, nivel);
        } else if (tipoEnum == Tipo.GASTRONOMICA) {
            nueva = new Gastronomica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl, vestimenta);
        } else {
            nueva = new Turistica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl,
                    vehiculo != null && vehiculo);
        }

        nueva.setLatitud(latitud);
        nueva.setLongitud(longitud);
        actividadRepository.save(nueva);
        return "redirect:/admin/actividades?success=true";
    }

    // ── Eliminar actividad ─────────────────────────────────────────────────────
    @PostMapping("/eliminar-actividad")
    public String eliminarActividad(@RequestParam(required = false) Long id) {
        if (id != null) actividadRepository.deleteById(id);
        return "redirect:/admin/actividades?deleted=true";
    }

    // Alias para compatibilidad con el panel antiguo
    @PostMapping("/eliminar")
    public String eliminarAlias(@RequestParam(required = false) Long id) {
        return eliminarActividad(id);
    }

    // ── Crear cuenta empresa ──────────────────────────────────────────────────
    @PostMapping("/empresas/crear")
    public String crearEmpresa(@RequestParam String nombre,
                               @RequestParam String email,
                               @RequestParam String password) {
        nombre = nombre != null ? nombre.trim() : "";
        email  = email  != null ? email.trim().toLowerCase() : "";

        if (nombre.isEmpty() || nombre.length() > 100)
            return "redirect:/admin/empresas?errorEmpresa=nombre";
        if (email.isEmpty() || !EMAIL_RE.matcher(email).matches())
            return "redirect:/admin/empresas?errorEmpresa=email_invalido";
        if (password == null || password.length() < 8)
            return "redirect:/admin/empresas?errorEmpresa=password_debil";
        if (usuarioRepository.findByEmail(email).isPresent())
            return "redirect:/admin/empresas?errorEmpresa=email_en_uso";

        Usuario empresa = new Usuario();
        empresa.setNombre(nombre);
        empresa.setEmail(email);
        empresa.setPassword(passwordEncoder.encode(password));
        empresa.setRol("ROLE_EMPRESA");
        empresa.setProveedor("LOCAL");
        usuarioRepository.save(empresa);
        return "redirect:/admin/empresas?createdEmpresa=true";
    }

    // ── Gestión de usuarios ────────────────────────────────────────────────────
    @PostMapping("/usuarios/eliminar")
    public String eliminarUsuario(@RequestParam(required = false) Long id) {
        if (id != null) usuarioRepository.deleteById(id);
        return "redirect:/admin/usuarios?deleted=true";
    }

    @PostMapping("/usuarios/cambiar-rol")
    public String cambiarRol(@RequestParam(required = false) Long id,
                             @RequestParam String nuevoRol) {
        if (id == null) return "redirect:/admin/usuarios";
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setRol(nuevoRol);
            usuarioRepository.save(u);
        });
        return "redirect:/admin/usuarios?rolChanged=true";
    }
}
