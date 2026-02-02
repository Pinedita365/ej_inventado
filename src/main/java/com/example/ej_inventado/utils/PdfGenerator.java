package com.example.ej_inventado.utils;

import com.example.ej_inventado.clases.Actividad;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.ByteArrayOutputStream;

public class PdfGenerator {
    public static byte[] generarItinerarioPdf(Actividad[] plan, String nombreUsuario) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("ITINERARIO DE VIAJE - TRIPING").setFontSize(20).setBold());
        document.add(new Paragraph("Viajero: " + nombreUsuario));
        document.add(new Paragraph("--------------------------------------------------"));

        for (int i = 0; i < plan.length; i++) {
            String actividadInfo = (plan[i] != null) ? 
                plan[i].getNombre() + " (" + plan[i].getCiudad() + ")" : "Día libre";
            document.add(new Paragraph("Día " + (i + 1) + ": " + actividadInfo));
        }

        document.close();
        return out.toByteArray();
    }
}