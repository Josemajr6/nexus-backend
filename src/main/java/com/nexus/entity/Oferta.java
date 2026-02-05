package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Oferta extends DomainEntity {

    @NotBlank
    private String titulo;  

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank
    private String tienda;
    
    private Double precioOriginal;
    private Double precioOferta;
    
    // URL EXTERNA del chollo (Amazon, AliExpress, etc.) - ESENCIAL
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String urlOferta;
    
    // Imagen principal OBLIGATORIA
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String imagenPrincipal;
    
    // Galería de imágenes OPCIONAL (máximo 4)
    @ElementCollection
    @CollectionTable(name = "oferta_galeria", joinColumns = @JoinColumn(name = "oferta_id"))
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private List<String> galeriaImagenes = new ArrayList<>();
    
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaPublicacion;
    
    private String categoria;
    private Boolean esActiva;
    
    // ⚡ SISTEMA SPARK (Nuevo concepto único)
    private Integer sparkCount;      // Votos positivos (⚡ Spark)
    private Integer dripCount;       // Votos negativos (💧 Drip)
    
    // Métricas
    private Integer numeroComentarios;
    private Integer numeroVistas;
    private Integer numeroCompartidos;
    
    // Badge automático según Spark Score
    @Enumerated(EnumType.STRING)
    private BadgeOferta badge;
    
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;
    
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comentario> comentarios = new ArrayList<>();
    
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SparkVoto> votos = new ArrayList<>();

    public Oferta() {
        super();
        this.fechaPublicacion = LocalDateTime.now();
        this.esActiva = true;
        this.sparkCount = 0;
        this.dripCount = 0;
        this.numeroComentarios = 0;
        this.numeroVistas = 0;
        this.numeroCompartidos = 0;
        this.badge = BadgeOferta.NORMAL;
    }

    // ⚡ MÉTODOS DEL SISTEMA SPARK
    
    public Integer getSparkScore() {
        return this.sparkCount - this.dripCount;
    }
    
    public Double getApprovalRate() {
        int total = this.sparkCount + this.dripCount;
        if (total == 0) return 0.0;
        return (this.sparkCount.doubleValue() / total) * 100;
    }
    
    public void actualizarBadge() {
        int score = getSparkScore();
        
        if (score >= 100) {
            this.badge = BadgeOferta.LEGENDARY;
        } else if (score >= 50) {
            this.badge = BadgeOferta.FIRE;
        } else if (score >= 20) {
            this.badge = BadgeOferta.HOT;
        } else if (score >= 5) {
            this.badge = BadgeOferta.TRENDING;
        } else if (score <= -10) {
            this.badge = BadgeOferta.EXPIRED;
        } else {
            this.badge = BadgeOferta.NORMAL;
        }
    }
    
    public void incrementarSpark() {
        this.sparkCount++;
        actualizarBadge();
    }
    
    public void decrementarSpark() {
        if (this.sparkCount > 0) this.sparkCount--;
        actualizarBadge();
    }
    
    public void incrementarDrip() {
        this.dripCount++;
        actualizarBadge();
    }
    
    public void decrementarDrip() {
        if (this.dripCount > 0) this.dripCount--;
        actualizarBadge();
    }
    
    public void incrementarVistas() {
        this.numeroVistas++;
    }
    
    public void incrementarCompartidos() {
        this.numeroCompartidos++;
    }
    
    public void actualizarNumeroComentarios() {
        this.numeroComentarios = this.comentarios.size();
    }
    
    public Double getPorcentajeDescuento() {
        if (precioOriginal == null || precioOriginal <= 0 || precioOferta >= precioOriginal) {
            return 0.0;
        }
        return ((precioOriginal - precioOferta) / precioOriginal) * 100;
    }

    public void addImagenGaleria(String url) {
        if (galeriaImagenes.size() < 4) {
            galeriaImagenes.add(url);
        } else {
            throw new IllegalStateException("Máximo 4 imágenes en la galería");
        }
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }

    public Double getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(Double precioOriginal) { this.precioOriginal = precioOriginal; }

    public Double getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(Double precioOferta) { this.precioOferta = precioOferta; }

    public String getUrlOferta() { return urlOferta; }
    public void setUrlOferta(String urlOferta) { this.urlOferta = urlOferta; }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public List<String> getGaleriaImagenes() { return galeriaImagenes; }
    public void setGaleriaImagenes(List<String> galeriaImagenes) { this.galeriaImagenes = galeriaImagenes; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getEsActiva() { return esActiva; }
    public void setEsActiva(Boolean esActiva) { this.esActiva = esActiva; }

    public Integer getSparkCount() { return sparkCount; }
    public void setSparkCount(Integer sparkCount) { this.sparkCount = sparkCount; }

    public Integer getDripCount() { return dripCount; }
    public void setDripCount(Integer dripCount) { this.dripCount = dripCount; }

    public Integer getNumeroComentarios() { return numeroComentarios; }
    public void setNumeroComentarios(Integer numeroComentarios) { this.numeroComentarios = numeroComentarios; }

    public Integer getNumeroVistas() { return numeroVistas; }
    public void setNumeroVistas(Integer numeroVistas) { this.numeroVistas = numeroVistas; }

    public Integer getNumeroCompartidos() { return numeroCompartidos; }
    public void setNumeroCompartidos(Integer numeroCompartidos) { this.numeroCompartidos = numeroCompartidos; }

    public BadgeOferta getBadge() { return badge; }
    public void setBadge(BadgeOferta badge) { this.badge = badge; }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }

    public List<Comentario> getComentarios() { return comentarios; }
    public void setComentarios(List<Comentario> comentarios) { this.comentarios = comentarios; }

    public List<SparkVoto> getVotos() { return votos; }
    public void setVotos(List<SparkVoto> votos) { this.votos = votos; }
}