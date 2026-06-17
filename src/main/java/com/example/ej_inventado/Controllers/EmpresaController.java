package com.example.ej_inventado.Controllers;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
import com.example.ej_inventado.repositories.ActividadRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.CloudinaryService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;

    public EmpresaController(ActividadRepository actividadRepository,
                             UsuarioRepository usuarioRepository,
                             CloudinaryService cloudinaryService) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository   = usuarioRepository;
        this.cloudinaryService   = cloudinaryService;
    }

    @GetMapping({ "", "/panel", "/actividades" })
    public String pageActividades(Authentication auth, HttpSession session, Model model) {
        String email = resolveEmail(auth);
        session.setAttribute("nombre", resolveName(auth, email));
        model.addAttribute("misActividades", actividadRepository.findByCreadorEmail(email));
        model.addAttribute("email", email);
        return "empresa/actividades";
    }

    @GetMapping("/nueva")
    public String pageNueva(Authentication auth, HttpSession session) {
        String email = resolveEmail(auth);
        session.setAttribute("nombre", resolveName(auth, email));
        return "empresa/nueva";
    }

    @PostMapping("/guardar")
    public String guardar(
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
            @RequestParam(required = false) Double longitud,
            Authentication auth) throws IOException {

        nombre      = nombre      != null ? nombre.trim()     : "";
        ciudad      = ciudad      != null ? ciudad.trim()      : "";
        descripcion = descripcion != null ? descripcion.trim() : "";

        if (nombre.isEmpty() || nombre.length() > 100)
            return "redirect:/empresa/actividades?errorGuardar=nombre";
        if (ciudad.isEmpty() || ciudad.length() > 100)
            return "redirect:/empresa/actividades?errorGuardar=ciudad";
        if (precio < 0 || precio > 10000)
            return "redirect:/empresa/actividades?errorGuardar=precio";
        if (duracion < 1 || duracion > 1440)
            return "redirect:/empresa/actividades?errorGuardar=duracion";
        if (descripcion.isEmpty() || descripcion.length() > 500)
            return "redirect:/empresa/actividades?errorGuardar=descripcion";
        if (imagen == null || imagen.isEmpty())
            return "redirect:/empresa/actividades?errorGuardar=imagen";

        Tipo tipoEnum;
        try { tipoEnum = Tipo.valueOf(tipo.toUpperCase()); }
        catch (Exception e) { return "redirect:/empresa/actividades?errorGuardar=tipo"; }

        String email = resolveEmail(auth);
        String imgUrl = cloudinaryService.uploadImage(imagen);

        com.example.ej_inventado.clases.Actividad nueva;
        if (tipoEnum == Tipo.DEPORTIVA) {
            Nivel nivel = (nivelStr != null && !nivelStr.isEmpty()) ? Nivel.valueOf(nivelStr) : Nivel.Bajo;
            nueva = new Deportiva(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl, nivel);
        } else if (tipoEnum == Tipo.GASTRONOMICA) {
            nueva = new Gastronomica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl, vestimenta);
        } else {
            nueva = new Turistica(tipoEnum, duracion, precio, ciudad, nombre, descripcion, imgUrl,
                    vehiculo != null && vehiculo);
        }

        nueva.setCreadorEmail(email);
        nueva.setLatitud(latitud);
        nueva.setLongitud(longitud);
        actividadRepository.save(nueva);

        return "redirect:/empresa/actividades?success=true";
    }

    @PostMapping("/editar")
    public String editar(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String ciudad,
            @RequestParam int precio,
            @RequestParam int duracion,
            @RequestParam String descripcion,
            @RequestParam(required = false) MultipartFile imagen,
            @RequestParam(required = false) String nivelStr,
            @RequestParam(required = false) String vestimenta,
            @RequestParam(required = false) Boolean vehiculo,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            Authentication auth) throws IOException {

        if (id == null) return "redirect:/empresa/actividades";
        String email = resolveEmail(auth);
        Actividad act = actividadRepository.findById(id).orElse(null);
        if (act == null || !email.equals(act.getCreadorEmail())) {
            return "redirect:/empresa/actividades?errorEditar=true";
        }

        nombre      = nombre      != null ? nombre.trim()     : "";
        ciudad      = ciudad      != null ? ciudad.trim()      : "";
        descripcion = descripcion != null ? descripcion.trim() : "";

        if (nombre.isEmpty() || nombre.length() > 100)
            return "redirect:/empresa/actividades?errorGuardar=nombre";
        if (ciudad.isEmpty() || ciudad.length() > 100)
            return "redirect:/empresa/actividades?errorGuardar=ciudad";
        if (precio < 0 || precio > 10000)
            return "redirect:/empresa/actividades?errorGuardar=precio";
        if (duracion < 1 || duracion > 1440)
            return "redirect:/empresa/actividades?errorGuardar=duracion";
        if (descripcion.isEmpty() || descripcion.length() > 500)
            return "redirect:/empresa/actividades?errorGuardar=descripcion";

        act.setNombre(nombre);
        act.setCiudad(ciudad);
        act.setPrecio(precio);
        act.setDuracion(duracion);
        act.setDescripcion(descripcion);
        act.setLatitud(latitud);
        act.setLongitud(longitud);

        if (imagen != null && !imagen.isEmpty()) {
            act.setImg(cloudinaryService.uploadImage(imagen));
        }

        if (act instanceof Deportiva dep && nivelStr != null && !nivelStr.isEmpty()) {
            dep.setNivel(Nivel.valueOf(nivelStr));
        } else if (act instanceof Gastronomica gas && vestimenta != null) {
            gas.setVestimenta(vestimenta);
        } else if (act instanceof Turistica tur) {
            tur.setVehiculo(vehiculo != null && vehiculo);
        }

        actividadRepository.save(act);
        return "redirect:/empresa/actividades?editado=true";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam(required = false) Long id, Authentication auth) {
        if (id == null) return "redirect:/empresa/panel";
        String email = resolveEmail(auth);
        actividadRepository.findById(id).ifPresent(act -> {
            if (email.equals(act.getCreadorEmail())) {
                actividadRepository.delete(act);
            }
        });
        return "redirect:/empresa/actividades?deleted=true";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String resolveEmail(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken token) {
            Object e = token.getPrincipal().getAttributes().get("email");
            if (e != null) return e.toString();
            Object l = token.getPrincipal().getAttributes().get("login");
            return l != null ? l + "@github.com" : "";
        }
        return auth.getName();
    }

    private String resolveName(Authentication auth, String email) {
        if (auth instanceof OAuth2AuthenticationToken token) {
            Object n = token.getPrincipal().getAttributes().get("name");
            if (n != null) return n.toString();
        }
        return usuarioRepository.findByEmail(email)
                .map(u -> u.getNombre()).orElse(email);
    }
}
