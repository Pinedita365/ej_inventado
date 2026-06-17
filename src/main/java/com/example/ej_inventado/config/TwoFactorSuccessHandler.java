package com.example.ej_inventado.config;

import java.io.IOException;
import java.security.SecureRandom;

import com.example.ej_inventado.repositories.UsuarioRepository;
import com.example.ej_inventado.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class TwoFactorSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public TwoFactorSuccessHandler(UsuarioRepository usuarioRepository,
                                   EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService      = emailService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication auth) throws IOException {
        String email = auth.getName();
        HttpSession session = request.getSession();

        String codigo = String.format("%06d", random.nextInt(1_000_000));
        long expiry   = System.currentTimeMillis() + 10L * 60 * 1000; // 10 min

        session.setAttribute("2fa_pending", true);
        session.setAttribute("2fa_code",    codigo);
        session.setAttribute("2fa_expiry",  expiry);
        session.setAttribute("2fa_email",   email);

        String nombre = usuarioRepository.findByEmail(email)
                .map(u -> u.getNombre()).orElse(email);

        try {
            emailService.enviarCodigo2FA(email, codigo, nombre);
            response.sendRedirect(request.getContextPath() + "/verificar-2fa");
        } catch (MessagingException ex) {
            // Email falló: el usuario sigue bloqueado por 2fa_pending.
            // Puede usar el botón "Reenviar código" cuando el servidor de correo esté disponible.
            response.sendRedirect(request.getContextPath() + "/verificar-2fa?emailError=true");
        }
    }
}
