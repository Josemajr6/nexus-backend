package com.nexus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

/**
 * Servicio de almacenamiento de imagenes y archivos usando Cloudinary.
 * Suprime el warning de unchecked cast usando @SuppressWarnings.
 */
@Service
public class StorageService {

    private final Cloudinary cloudinary;

    public StorageService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}")    String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key",    apiKey,
            "api_secret", apiSecret,
            "secure",     true
        ));
    }

    /**
     * Sube una imagen y devuelve la URL segura de Cloudinary.
     * Retorna null si hay error (el llamador decide si es critico).
     */
    public String subirImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = (Map<String, Object>) cloudinary.uploader()
                .upload(archivo.getBytes(), ObjectUtils.asMap(
                    "folder",          "nexus",
                    "resource_type",   "image",
                    "quality",         "auto:good",
                    "fetch_format",    "auto"
                ));
            return (String) resultado.get("secure_url");
        } catch (Exception e) {
            System.err.println("Error subiendo imagen a Cloudinary: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sube un audio (mensajes de voz del chat) y devuelve la URL.
     */
    public String subirAudio(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = (Map<String, Object>) cloudinary.uploader()
                .upload(archivo.getBytes(), ObjectUtils.asMap(
                    "folder",        "nexus/audio",
                    "resource_type", "video"  // Cloudinary usa "video" para audio tambien
                ));
            return (String) resultado.get("secure_url");
        } catch (Exception e) {
            System.err.println("Error subiendo audio a Cloudinary: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sube multiples imagenes y devuelve la lista de URLs.
     */
    public List<String> subirImagenes(List<MultipartFile> archivos) {
        List<String> urls = new ArrayList<>();
        if (archivos == null) return urls;
        for (MultipartFile archivo : archivos) {
            String url = subirImagen(archivo);
            if (url != null) urls.add(url);
        }
        return urls;
    }

    /**
     * Elimina un recurso de Cloudinary por su public_id.
     */
    public boolean eliminar(String publicId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = (Map<String, Object>)
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(resultado.get("result"));
        } catch (Exception e) {
            System.err.println("Error eliminando de Cloudinary: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     * Util para llamar a eliminar() con la URL completa.
     */
    public String extraerPublicId(String url) {
        if (url == null || !url.contains("cloudinary")) return null;
        try {
            String[] partes = url.split("/upload/");
            if (partes.length < 2) return null;
            String sinVersion = partes[1].replaceAll("v\\d+/", "");
            int puntoExtension = sinVersion.lastIndexOf('.');
            return puntoExtension > 0 ? sinVersion.substring(0, puntoExtension) : sinVersion;
        } catch (Exception e) {
            return null;
        }
    }
}