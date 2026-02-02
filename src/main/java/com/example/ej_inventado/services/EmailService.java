package com.example.ej_inventado.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarItinerarioConPdf(String destinatario, String nombreUsuario, byte[] pdfBytes) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        // Añade el encoding UTF-8 explícitamente
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("javier.pineda-enrique@gmail.com");
        helper.setTo(destinatario);
        helper.setSubject("🎒 Tu Itinerario de Viaje - TriPing");
        helper.setText("Hola " + nombreUsuario + ", adjuntamos tu PDF.", false); // false para que sea texto plano

        helper.addAttachment("Itinerario_TriPing.pdf", new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }
}