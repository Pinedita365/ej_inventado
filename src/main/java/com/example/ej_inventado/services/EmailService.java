package com.example.ej_inventado.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void setFrom(MimeMessageHelper helper) throws MessagingException {
        try {
            helper.setFrom(new InternetAddress(fromAddress, "TriPing", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(fromAddress);
        }
    }

    // ── Itinerario en PDF ─────────────────────────────────────────────────────
    public void enviarItinerarioConPdf(String destinatario, String nombreUsuario, byte[] pdfBytes)
            throws MessagingException {

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        setFrom(helper);
        helper.setTo(destinatario);
        helper.setSubject("Tu itinerario de viaje — TriPing");
        helper.setText("""
                <html><body style="font-family:sans-serif;color:#1A2530">
                <h2 style="color:#023C40">¡Tu itinerario está listo! 🗺️</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>Adjuntamos tu itinerario de viaje en PDF. ¡Que disfrutes la aventura!</p>
                <br><p style="color:#6B7280;font-size:12px">— El equipo de TriPing</p>
                </body></html>
                """.formatted(nombreUsuario), true);

        helper.addAttachment("Itinerario_TriPing.pdf",
                () -> new java.io.ByteArrayInputStream(pdfBytes),
                "application/pdf");

        mailSender.send(msg);
    }

    // ── Código de verificación en dos pasos ───────────────────────────────────
    public void enviarCodigo2FA(String destinatario, String codigo, String nombre)
            throws MessagingException {

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");

        setFrom(helper);
        helper.setTo(destinatario);
        helper.setSubject("Tu código de verificación — TriPing");
        helper.setText("""
                <html><body style="font-family:sans-serif;color:#1A2530;max-width:480px;margin:auto">
                <div style="background:#023C40;padding:24px;border-radius:12px 12px 0 0;text-align:center">
                  <h1 style="color:#78FFD6;margin:0;font-size:1.4rem">🌍 TriPing</h1>
                </div>
                <div style="background:#fff;padding:32px;border:1px solid #DDE3EA;border-top:none;border-radius:0 0 12px 12px">
                  <h2 style="margin:0 0 8px">Código de verificación</h2>
                  <p>Hola <strong>%s</strong>, introduce este código para completar tu acceso:</p>
                  <div style="background:#F4F7F9;border:2px dashed #023C40;border-radius:12px;text-align:center;padding:20px;margin:20px 0">
                    <span style="font-size:2.5rem;font-weight:800;letter-spacing:12px;color:#023C40">%s</span>
                  </div>
                  <p style="color:#6B7280;font-size:13px">⏱️ Este código expira en <strong>10 minutos</strong>.</p>
                  <p style="color:#6B7280;font-size:13px">Si no has iniciado sesión, ignora este mensaje.</p>
                </div>
                </body></html>
                """.formatted(nombre, codigo), true);

        mailSender.send(msg);
    }

    // ── Recuperación de contraseña ────────────────────────────────────────────
    public void enviarRecuperacionPassword(String destinatario, String nombre, String enlace)
            throws MessagingException {

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");

        setFrom(helper);
        helper.setTo(destinatario);
        helper.setSubject("Recupera tu contraseña — TriPing");
        helper.setText("""
                <html><body style="font-family:sans-serif;color:#1A2530;max-width:480px;margin:auto">
                <div style="background:#023C40;padding:24px;border-radius:12px 12px 0 0;text-align:center">
                  <h1 style="color:#78FFD6;margin:0;font-size:1.4rem">🌍 TriPing</h1>
                </div>
                <div style="background:#fff;padding:32px;border:1px solid #DDE3EA;border-top:none;border-radius:0 0 12px 12px">
                  <h2 style="margin:0 0 8px">Recupera tu contraseña</h2>
                  <p>Hola <strong>%s</strong>, hemos recibido una solicitud para restablecer tu contraseña.</p>
                  <div style="text-align:center;margin:24px 0">
                    <a href="%s" style="background:#0AD3FF;color:#fff;padding:14px 28px;border-radius:10px;text-decoration:none;font-weight:700;font-size:1rem">
                      Restablecer contraseña →
                    </a>
                  </div>
                  <p style="color:#6B7280;font-size:13px">⏱️ El enlace expira en <strong>1 hora</strong>.</p>
                  <p style="color:#6B7280;font-size:13px">Si no solicitaste esto, puedes ignorar este mensaje.</p>
                  <hr style="border:none;border-top:1px solid #DDE3EA;margin:20px 0">
                  <p style="color:#9ca3af;font-size:11px;word-break:break-all">%s</p>
                </div>
                </body></html>
                """.formatted(nombre, enlace, enlace), true);

        mailSender.send(msg);
    }
}
