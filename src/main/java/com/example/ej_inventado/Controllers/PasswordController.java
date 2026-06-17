package com.example.ej_inventado.Controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ej_inventado.clases.PasswordResetToken;
import com.example.ej_inventado.repositories.PasswordResetTokenRepository;
import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PasswordController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordController(UsuarioRepository usuarioRepository,
                              PasswordResetTokenRepository tokenRepository,
                              EmailService emailService,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository   = tokenRepository;
        this.emailService      = emailService;
        this.passwordEncoder   = passwordEncoder;
    }

    // ══ RECUPERACIÓN DE CONTRASEÑA ════════════════════════════════════════════

    @GetMapping("/recuperar-password")
    public String recuperarForm() {
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String recuperarEnviar(@RequestParam String email, Model model) {
        usuarioRepository.findByEmail(email.trim().toLowerCase()).ifPresent(u -> {
            // Borrar tokens anteriores del mismo email
            tokenRepository.deleteByEmail(u.getEmail());

            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(
                    token,
                    u.getEmail(),
                    LocalDateTime.now().plusHours(1)
            ));

            String enlace = baseUrl + "/reset-password?token=" + token;
            try {
                emailService.enviarRecuperacionPassword(u.getEmail(), u.getNombre(), enlace);
            } catch (Exception ignored) {}
        });

        // Siempre mostramos éxito para no revelar si el email existe
        model.addAttribute("enviado", true);
        return "recuperar-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model) {
        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.isExpired()) {
                        model.addAttribute("error", "El enlace ha caducado. Solicita uno nuevo.");
                        return "reset-password";
                    }
                    model.addAttribute("token", token);
                    return "reset-password";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Enlace inválido o ya utilizado.");
                    return "reset-password";
                });
    }

    @PostMapping("/reset-password")
    public String resetGuardar(@RequestParam String token,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "reset-password";
        }
        if (password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "reset-password";
        }

        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.isExpired()) {
                        model.addAttribute("error", "El enlace ha caducado. Solicita uno nuevo.");
                        return "reset-password";
                    }
                    usuarioRepository.findByEmail(t.getEmail()).ifPresent(u -> {
                        u.setPassword(passwordEncoder.encode(password));
                        usuarioRepository.save(u);
                    });
                    tokenRepository.delete(t);
                    return "redirect:/login?reset=true";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Enlace inválido o ya utilizado.");
                    return "reset-password";
                });
    }

    // ══ VERIFICACIÓN EN DOS PASOS ═════════════════════════════════════════════

    @GetMapping("/verificar-2fa")
    public String verificar2faForm(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("2fa_pending"))) {
            return "redirect:/";
        }
        String email = (String) session.getAttribute("2fa_email");
        if (email != null) {
            // Mostrar solo últimos caracteres: ****@gmail.com
            int at = email.indexOf('@');
            String masked = (at > 2)
                    ? "****" + email.substring(at)
                    : "****";
            model.addAttribute("emailMasked", masked);
        }
        Long expiry = (Long) session.getAttribute("2fa_expiry");
        if (expiry != null) {
            long segundosRestantes = (expiry - System.currentTimeMillis()) / 1000;
            model.addAttribute("segundosRestantes", Math.max(0, segundosRestantes));
        }
        return "verificar-2fa";
    }

    @PostMapping("/verificar-2fa")
    public String verificar2fa(@RequestParam String codigo,
                               HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("2fa_pending"))) {
            return "redirect:/";
        }

        String codigoGuardado = (String) session.getAttribute("2fa_code");
        Long expiry           = (Long)   session.getAttribute("2fa_expiry");

        boolean expirado = expiry == null || System.currentTimeMillis() > expiry;
        boolean correcto = codigoGuardado != null && codigoGuardado.equals(codigo.trim());

        if (!expirado && correcto) {
            session.removeAttribute("2fa_pending");
            session.removeAttribute("2fa_code");
            session.removeAttribute("2fa_expiry");
            session.removeAttribute("2fa_email");
            return "redirect:/";
        }

        return expirado
                ? "redirect:/verificar-2fa?expired=true"
                : "redirect:/verificar-2fa?error=true";
    }

    @PostMapping("/reenviar-2fa")
    public String reenviar2fa(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("2fa_pending"))) {
            return "redirect:/";
        }
        String email  = (String) session.getAttribute("2fa_email");
        if (email == null) return "redirect:/login";

        String nombre = usuarioRepository.findByEmail(email)
                .map(u -> u.getNombre()).orElse(email);

        String nuevoCodigo = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        long   nuevoExpiry = System.currentTimeMillis() + 10L * 60 * 1000;

        session.setAttribute("2fa_code",   nuevoCodigo);
        session.setAttribute("2fa_expiry", nuevoExpiry);

        try {
            emailService.enviarCodigo2FA(email, nuevoCodigo, nombre);
        } catch (Exception ignored) {}

        return "redirect:/verificar-2fa?reenviado=true";
    }
}
