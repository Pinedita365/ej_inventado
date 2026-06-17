package com.example.ej_inventado.Controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import com.example.ej_inventado.clases.Resena;
import com.example.ej_inventado.repositories.ResenaRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;

@Controller
public class LandingController {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;

    public LandingController(ResenaRepository resenaRepository,
                             UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String landing(Authentication auth, HttpSession session, Model model) {
        sincronizarSesion(auth, session);
        List<Resena> resenas = resenaRepository.findTop12ByAprobadaOrderByFechaDesc(true);
        model.addAttribute("resenas", resenas);
        model.addAttribute("totalResenas", resenaRepository.countByAprobada(true));
        return "landing";
    }

    @PostMapping("/resenas/crear")
    public String crearResena(@RequestParam String texto,
                              @RequestParam int estrellas,
                              @RequestParam(required = false) String ciudad,
                              Authentication auth) {
        texto = texto != null ? texto.trim() : "";
        if (texto.isEmpty() || texto.length() > 600)
            return "redirect:/?errorResena=texto";

        estrellas = Math.min(5, Math.max(1, estrellas));

        String ciudadLimpia = (ciudad != null && !ciudad.isBlank())
                ? ciudad.trim().substring(0, Math.min(ciudad.trim().length(), 100))
                : null;

        String email  = auth.getName();
        String nombre = usuarioRepository.findByEmail(email)
                .map(u -> u.getNombre()).orElse(email);

        Resena r = new Resena();
        r.setNombreAutor(nombre);
        r.setTexto(texto);
        r.setEstrellas(estrellas);
        r.setCiudad(ciudadLimpia);
        r.setFecha(LocalDateTime.now());
        r.setAprobada(true);
        resenaRepository.save(r);

        return "redirect:/#resenas";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/sobre-nosotros")
    public String sobreNosotros() { return "legal/sobre-nosotros"; }

    @GetMapping("/cookies")
    public String cookies() { return "legal/cookies"; }

    @GetMapping("/terminos")
    public String terminos() { return "legal/terminos"; }

    @GetMapping("/privacidad")
    public String privacidad() { return "legal/privacidad"; }

    @GetMapping("/clientes")
    public String clientes() { return "legal/clientes"; }

    private void sincronizarSesion(Authentication auth, HttpSession session) {
        if (auth == null || !auth.isAuthenticated()) return;
        if (session.getAttribute("nombre") != null) return; // ya sincronizado

        String email = auth.getName();
        String nombre = email;

        if (auth instanceof OAuth2AuthenticationToken token) {
            var attrs = token.getPrincipal().getAttributes();
            if (attrs.get("email")  != null) email  = attrs.get("email").toString();
            if (attrs.get("name")   != null) nombre = attrs.get("name").toString();
            else if (attrs.get("login") != null) nombre = attrs.get("login").toString();
        }

        final String finalEmail = email;
        session.setAttribute("nombre", nombre);
        usuarioRepository.findByEmail(finalEmail).ifPresent(u -> {
            session.setAttribute("nombre",     u.getNombre());
            session.setAttribute("rolUsuario", u.getRol());
            session.setAttribute("fotoPerfil", u.getFotoPerfil());
        });
    }
}
