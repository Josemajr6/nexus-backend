package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oferta", indexes = {
    @Index(name = "idx_oferta_activa",    columnList = "esActiva"),
    @Index(name = "idx_oferta_categoria", columnList = "categoria_id"),
    @Index(name = "idx_oferta_spark",     columnList = "sparkCount")
})
public class Oferta extends DomainEntity {

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precioOferta = 0.0;

    private Double precioOriginal;
    private String tienda;

    /** URL externa de la oferta (Amazon, PCComponentes, etc.) */
    @Column(name = "url_oferta", columnDefinition = "TEXT")
    private String urlOferta;

    // ---- Categoria -------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // ---- Imagenes --------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String imagenPrincipal;

    @ElementCollection
    @CollectionTable(name = "oferta_imagenes",
                     joinColumns = @JoinColumn(name = "oferta_id"))
    @Column(name = "url", columnDefinition = "TEXT")
    private List<String> galeriaImagenes = new ArrayList<>();

    // ---- Publicador ------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    // ---- Estado ----------------------------------------------------------
    @Column(nullable = false)
    private Boolean esActiva = true;

    @Enumerated(EnumType.STRING)
    private BadgeOferta badge;

    // ---- Fechas ----------------------------------------------------------
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaExpiracion;

    // ---- Contadores de votos ---------------------------------------------
    @Column(nullable = false)
    private Integer sparkCount = 0;

    @Column(nullable = false)
    private Integer dripCount = 0;

    // ---- Contadores de interaccion ---------------------------------------
    @Column(nullable = false)
    private Integer numeroVistas = 0;

    @Column(nullable = false)
    private Integer numeroCompartidos = 0;

    @Column(nullable = false)
    private Integer numeroComentarios = 0;

    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion  == null) fechaPublicacion  = LocalDateTime.now();
        if (sparkCount        == null) sparkCount        = 0;
        if (dripCount         == null) dripCount         = 0;
        if (numeroVistas      == null) numeroVistas      = 0;
        if (numeroCompartidos == null) numeroCompartidos = 0;
        if (numeroComentarios == null) numeroComentarios = 0;
        if (esActiva          == null) esActiva          = true;
        if (galeriaImagenes   == null) galeriaImagenes   = new ArrayList<>();
    }

    // ---- Getters / Setters -----------------------------------------------

    public String  getTitulo()                             { return titulo; }
    public void    setTitulo(String t)                     { this.titulo = t; }
    public String  getDescripcion()                        { return descripcion; }
    public void    setDescripcion(String d)                { this.descripcion = d; }
    public Double  getPrecioOferta()                       { return precioOferta; }
    public void    setPrecioOferta(Double p)               { this.precioOferta = p; }
    public Double  getPrecioOriginal()                     { return precioOriginal; }
    public void    setPrecioOriginal(Double p)             { this.precioOriginal = p; }
    public String  getTienda()                             { return tienda; }
    public void    setTienda(String t)                     { this.tienda = t; }
    public String  getUrlOferta()                          { return urlOferta; }
    public void    setUrlOferta(String u)                  { this.urlOferta = u; }
    public Categoria getCategoria()                        { return categoria; }
    public void    setCategoria(Categoria c)               { this.categoria = c; }
    public String  getImagenPrincipal()                    { return imagenPrincipal; }
    public void    setImagenPrincipal(String i)            { this.imagenPrincipal = i; }
    public List<String> getGaleriaImagenes()               { return galeriaImagenes; }
    public void    setGaleriaImagenes(List<String> l)      { this.galeriaImagenes = l; }
    public Actor   getActor()                              { return actor; }
    public void    setActor(Actor a)                       { this.actor = a; }
    public Boolean getEsActiva()                           { return esActiva; }
    public void    setEsActiva(Boolean a)                  { this.esActiva = a; }
    public BadgeOferta getBadge()                          { return badge; }
    public void    setBadge(BadgeOferta b)                 { this.badge = b; }
    public LocalDateTime getFechaPublicacion()             { return fechaPublicacion; }
    public void    setFechaPublicacion(LocalDateTime f)    { this.fechaPublicacion = f; }
    public LocalDateTime getFechaExpiracion()              { return fechaExpiracion; }
    public void    setFechaExpiracion(LocalDateTime f)     { this.fechaExpiracion = f; }
    public Integer getSparkCount()                         { return sparkCount; }
    public void    setSparkCount(Integer s)                { this.sparkCount = s; }
    public Integer getDripCount()                          { return dripCount; }
    public void    setDripCount(Integer d)                 { this.dripCount = d; }
    public Integer getNumeroVistas()                       { return numeroVistas; }
    public void    setNumeroVistas(int v)                  { this.numeroVistas = v; }
    public Integer getNumeroCompartidos()                  { return numeroCompartidos; }
    public void    setNumeroCompartidos(int c)             { this.numeroCompartidos = c; }
    public Integer getNumeroComentarios()                  { return numeroComentarios; }
    public void    setNumeroComentarios(Integer c)         { this.numeroComentarios = c; }

    // ---- Alias de compatibilidad (imagenesAdicionales -> galeriaImagenes) ----
    public List<String> getImagenesAdicionales()           { return galeriaImagenes; }
    public void    setImagenesAdicionales(List<String> l)  { this.galeriaImagenes = l; }

    // ---- Helpers ---------------------------------------------------------

    /** Anadir una imagen a la galeria (usado en controllers y PopulateDB) */
    public void addImagenGaleria(String url) {
        if (galeriaImagenes == null) galeriaImagenes = new ArrayList<>();
        galeriaImagenes.add(url);
    }

    /** Incrementa numeroComentarios en 1 (llamado por ComentarioService) */
    public void actualizarNumeroComentarios() {
        if (numeroComentarios == null) numeroComentarios = 0;
        this.numeroComentarios++;
    }

    /**
     * Recalcula el badge segun las reglas del negocio.
     * Llamar justo antes de save().
     */
    public void actualizarBadge() {
        if (precioOferta != null && precioOferta == 0.0) {
            this.badge = BadgeOferta.GRATUITA;
            return;
        }
        if (fechaExpiracion != null
                && fechaExpiracion.isBefore(LocalDateTime.now().plusHours(24))) {
            this.badge = BadgeOferta.EXPIRA_HOY;
            return;
        }
        if (fechaPublicacion != null
                && fechaPublicacion.isAfter(LocalDateTime.now().minusHours(1))) {
            this.badge = BadgeOferta.NUEVA;
            return;
        }
        double pct = getPorcentajeDescuento();
        if (pct >= 70)      this.badge = BadgeOferta.CHOLLAZO;
        else if (pct >= 40) this.badge = BadgeOferta.PORCENTAJE;
    }

    // ---- Calculados (@Transient) -----------------------------------------

    /** sparkCount - dripCount */
    @Transient
    public int getSparkScore() {
        int s = (sparkCount != null) ? sparkCount : 0;
        int d = (dripCount  != null) ? dripCount  : 0;
        return s - d;
    }

    @Transient
    public double getPorcentajeDescuento() {
        if (precioOriginal == null || precioOriginal <= 0 || precioOferta == null) return 0;
        return Math.round(((precioOriginal - precioOferta) / precioOriginal) * 100.0);
    }
}