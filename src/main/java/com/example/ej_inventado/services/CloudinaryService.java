package com.example.ej_inventado.services;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud_name:}") String name,
                             @Value("${cloudinary.api_key:}")    String key,
                             @Value("${cloudinary.api_secret:}") String secret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", name,
                "api_key",    key,
                "api_secret", secret,
                "secure",     true));
    }

    /** Sube una imagen de actividad a la carpeta triping/actividades */
    public String uploadImage(MultipartFile file) throws IOException {
        return upload(file, "triping/actividades");
    }

    /** Sube una foto de perfil de usuario a la carpeta triping/perfiles */
    public String uploadFotoPerfil(MultipartFile file) throws IOException {
        return upload(file, "triping/perfiles");
    }

    private String upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",          folder,
                        "resource_type",   "image",
                        "transformation",  ObjectUtils.asMap(
                                "quality", "auto",
                                "fetch_format", "auto")
                ));
        return uploadResult.get("secure_url").toString();
    }
}
