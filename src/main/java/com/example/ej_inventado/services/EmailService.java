package com.example.ej_inventado.services;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    // Lee la clave SG que pusiste en Render
    @Value("${SENDGRID_API_KEY}")
    private String apiKey;

    public void enviarItinerarioConPdf(String destinatario, String nombreUsuario, byte[] pdfBytes) throws Exception {
        // IMPORTANTE: Este correo debe ser el que verificaste en SendGrid
        Email from = new Email("javiercoco2201@gmail.com"); 
        Email to = new Email(destinatario);
        String subject = "🎒 Tu Itinerario de Viaje - TriPing";
        Content content = new Content("text/plain", "Hola " + nombreUsuario + ", adjuntamos tu PDF.");
        
        Mail mail = new Mail(from, subject, to, content);

        // --- Lógica para adjuntar el PDF ---
        Attachments attachments = new Attachments();
        // Convertimos el PDF a Base64 (necesario para la API)
        String base64Content = Base64.getEncoder().encodeToString(pdfBytes);
        
        attachments.setContent(base64Content);
        attachments.setType("application/pdf");
        attachments.setFilename("Itinerario_TriPing.pdf");
        attachments.setDisposition("attachment");
        mail.addAttachments(attachments);

        // Envío mediante HTTP POST (Puerto 443, no bloqueado)
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new IOException("Error de SendGrid: " + response.getBody());
            }
            System.out.println("¡Email enviado con éxito! Status: " + response.getStatusCode());
        } catch (IOException ex) {
            System.err.println("Error enviando el correo: " + ex.getMessage());
            throw ex;
        }
    }
}