package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Oferta extends DomainEntity {

    @NotBlank
    private String titulo;  

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String tienda;
    private double precioOriginal;
    private double precioOferta;
    
    // ✅ NUEVO: Imagen principal OBLIGATORIA (banner/foto destacada)
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String imagenPrincipal;
    
    // ✅ NUEVO: Galería de imágenes OPCIONAL (máximo 4 fotos extra)
    @ElementCollection
    @CollectionTable(name = "oferta_galeria", joinColumns = @JoinColumn(name = "oferta_id"))
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private List<String> galeriaImagenes = new ArrayList<>();
    
    private LocalDateTime fechaExpiracion;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    public Oferta() {
        super();
    }

    // ✅ NUEVO: Método para añadir imágenes a la galería con validación
    public void addImagenGaleria(String url) {
        if (galeriaImagenes.size() < 4) { // Máximo 4 imágenes extra
            galeriaImagenes.add(url);
        } else {
            throw new IllegalStateException("Máximo 4 imágenes en la galería de ofertas");
        }
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }

    public double getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(double precioOriginal) { this.precioOriginal = precioOriginal; }

    public double getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(double precioOferta) { this.precioOferta = precioOferta; }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public List<String> getGaleriaImagenes() { return galeriaImagenes; }
    public void setGaleriaImagenes(List<String> galeriaImagenes) { this.galeriaImagenes = galeriaImagenes; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }
}