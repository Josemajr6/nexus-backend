package com.nexus.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Servicio Cloudinary — soporta imágenes, vídeos y AUDIO (mensajes de voz).
 */
@Service
public class StorageService {

    private final Cloudinary cloudinary;

    public StorageService(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}")    String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret) {

        this.cloudinary = (cloudName.isEmpty() || apiKey.isEmpty()) ? null
            : new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret));
    }

    public String subirImagen(MultipartFile file) {
        return subir(file, "image", "nexus/imagenes", null);
    }

    public String subirVideo(MultipartFile file) {
        return subir(file, "video", "nexus/videos",
            ObjectUtils.asMap("quality", "auto",
                "eager", ObjectUtils.asMap("width", 400, "height", 300, "crop", "fill", "format", "jpg")));
    }

    /**
     * Sube un archivo de audio (webm/ogg/mp3) a Cloudinary.
     * Los mensajes de voz del chat se almacenan aquí.
     * Cloudinary almacena audio como recurso "video" (soporta ambos formatos).
     */
    public String subirAudio(MultipartFile file) {
        return subir(file, "video", "nexus/audios", null);
    }

    public void eliminarImagen(String url) { eliminar(url, "image"); }
    public void eliminarVideo(String url)  { eliminar(url, "video"); }
    public void eliminarAudio(String url)  { eliminar(url, "video"); }

    // ── Privados ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String subir(MultipartFile file, String resourceType, String folder, Map<String, Object> extras) {
        if (cloudinary == null) return "https://via.placeholder.com/400";
        try {
            Map<String, Object> opts = new java.util.HashMap<>();
            opts.put("resource_type", resourceType);
            opts.put("folder", folder);
            if (extras != null) opts.putAll(extras);
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), opts);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void eliminar(String url, String resourceType) {
        if (cloudinary == null || url == null || url.isBlank()) return;
        try {
            String publicId = extraerPublicId(url);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error eliminando de Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Extrae el public_id completo incluyendo carpeta.
     * FIX del bug original que solo extraía el último segmento de la URL.
     *
     * https://res.cloudinary.com/xyz/image/upload/v123/nexus/imagenes/abc.jpg
     *   → nexus/imagenes/abc
     */
    private String extraerPublicId(String url) {
        try {
            String[] partes = url.split("/upload/");
            if (partes.length < 2) return null;
            String resto = partes[1];
            if (resto.startsWith("v") && resto.contains("/"))
                resto = resto.substring(resto.indexOf("/") + 1);
            int dot = resto.lastIndexOf(".");
            return dot > 0 ? resto.substring(0, dot) : resto;
        } catch (Exception e) { return null; }
    }
}