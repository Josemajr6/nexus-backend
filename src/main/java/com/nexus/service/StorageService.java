package com.nexus.service;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class StorageService {

    private final Cloudinary cloudinary;

    public StorageService(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret) {
        
        if (cloudName.isEmpty() || apiKey.isEmpty()) {
            this.cloudinary = null;
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
        }
    }

    public String subirImagen(MultipartFile file) {
        if (cloudinary == null) return "https://via.placeholder.com/300";

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadResult.get("url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- LÓGICA DE ELIMINACIÓN ---
    public void eliminarImagen(String url) {
        if (cloudinary == null || url == null || url.isEmpty()) return;

        try {
            String publicId = obtenerPublicId(url);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("🗑️ Imagen eliminada de Cloudinary: " + publicId);
            }
        } catch (IOException e) {
            System.err.println("❌ Error al eliminar imagen de Cloudinary: " + e.getMessage());
        }
    }

    // Método auxiliar para extraer el ID de la URL
    private String obtenerPublicId(String url) {
        try {
            // Patrón para extraer lo que está después de la última '/' y antes del punto de extensión
            // Ejemplo URL: .../image/upload/v123456/sample.jpg -> ID: sample
            // Si usas carpetas, la lógica puede variar, pero esto funciona para subidas estándar.
            
            // Paso 1: Obtener el nombre del archivo con extensión (ej: sample.jpg)
            String filename = url.substring(url.lastIndexOf("/") + 1);
            
            // Paso 2: Quitar la extensión
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex > 0) {
                return filename.substring(0, dotIndex);
            }
            return filename;
        } catch (Exception e) {
            return null;
        }
    }
}