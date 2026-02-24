package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto", indexes = {
    @Index(name = "idx_producto_estado",     columnList = "estadoProducto"),
    @Index(name = "idx_producto_categoria",  columnList = "categoria_id"),
    @Index(name = "idx_producto_publicador", columnList = "publicador_id")
})
public class Producto extends DomainEntity {

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOferta tipoOferta = TipoOferta.VENTA;

    // ---- Categoria -------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String subcategoria;
    private String marca;
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProducto estadoProducto = EstadoProducto.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    private CondicionProducto condicion;

    private String ubicacion;

    @Column(nullable = false)
    private boolean admiteEnvio = false;

    private Double precioEnvio = 0.0;

    @Column(nullable = false)
    private boolean admiteNegociacion = true;

    // ---- Imagenes --------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String imagenPrincipal;

    @ElementCollection
    @CollectionTable(name = "producto_imagenes",
                     joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "url", columnDefinition = "TEXT")
    private List<String> galeriaImagenes = new ArrayList<>();

    // ---- Publicador ------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicador_id", nullable = false)
    private Actor publicador;

    private LocalDateTime fechaPublicacion;

    // ---- Constructores ---------------------------------------------------

    /** Constructor por defecto requerido por JPA */
    public Producto() {}

    /**
     * Constructor usado en PopulateDB y tests:
     *   new Producto(titulo, descripcion, precio, tipoOferta, usuario, imagenPrincipal)
     */
    public Producto(String titulo, String descripcion, double precio,
                    TipoOferta tipoOferta, Actor publicador, String imagenPrincipal) {
        this.titulo          = titulo;
        this.descripcion     = descripcion;
        this.precio          = precio;
        this.tipoOferta      = tipoOferta != null ? tipoOferta : TipoOferta.VENTA;
        this.publicador      = publicador;
        this.imagenPrincipal = imagenPrincipal;
        this.estadoProducto  = EstadoProducto.DISPONIBLE;
        this.galeriaImagenes = new ArrayList<>();
        this.fechaPublicacion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) fechaPublicacion = LocalDateTime.now();
        if (estadoProducto   == null) estadoProducto   = EstadoProducto.DISPONIBLE;
        if (tipoOferta       == null) tipoOferta       = TipoOferta.VENTA;
        if (galeriaImagenes  == null) galeriaImagenes  = new ArrayList<>();
    }

    // ---- Getters / Setters -----------------------------------------------

    public String  getTitulo()                             { return titulo; }
    public void    setTitulo(String t)                     { this.titulo = t; }
    public String  getDescripcion()                        { return descripcion; }
    public void    setDescripcion(String d)                { this.descripcion = d; }
    public Double  getPrecio()                             { return precio; }
    public void    setPrecio(Double p)                     { this.precio = p; }
    public TipoOferta getTipoOferta()                      { return tipoOferta; }
    public void    setTipoOferta(TipoOferta t)             { this.tipoOferta = t; }
    public Categoria getCategoria()                        { return categoria; }
    public void    setCategoria(Categoria c)               { this.categoria = c; }
    public String  getSubcategoria()                       { return subcategoria; }
    public void    setSubcategoria(String s)               { this.subcategoria = s; }
    public String  getMarca()                              { return marca; }
    public void    setMarca(String m)                      { this.marca = m; }
    public String  getModelo()                             { return modelo; }
    public void    setModelo(String m)                     { this.modelo = m; }
    public EstadoProducto getEstadoProducto()              { return estadoProducto; }
    public void    setEstadoProducto(EstadoProducto e)     { this.estadoProducto = e; }
    public CondicionProducto getCondicion()                { return condicion; }
    public void    setCondicion(CondicionProducto c)       { this.condicion = c; }
    public String  getUbicacion()                          { return ubicacion; }
    public void    setUbicacion(String u)                  { this.ubicacion = u; }
    public boolean isAdmiteEnvio()                         { return admiteEnvio; }
    public void    setAdmiteEnvio(boolean a)               { this.admiteEnvio = a; }
    public Double  getPrecioEnvio()                        { return precioEnvio; }
    public void    setPrecioEnvio(Double p)                { this.precioEnvio = p; }
    public boolean isAdmiteNegociacion()                   { return admiteNegociacion; }
    public void    setAdmiteNegociacion(boolean a)         { this.admiteNegociacion = a; }
    public String  getImagenPrincipal()                    { return imagenPrincipal; }
    public void    setImagenPrincipal(String i)            { this.imagenPrincipal = i; }
    public List<String> getGaleriaImagenes()               { return galeriaImagenes; }
    public void    setGaleriaImagenes(List<String> l)      { this.galeriaImagenes = l; }
    public Actor   getPublicador()                         { return publicador; }
    public void    setPublicador(Actor p)                  { this.publicador = p; }
    public LocalDateTime getFechaPublicacion()             { return fechaPublicacion; }
    public void    setFechaPublicacion(LocalDateTime f)    { this.fechaPublicacion = f; }

    /** Alias de compatibilidad */
    public List<String> getImagenesAdicionales()           { return galeriaImagenes; }
    public void    setImagenesAdicionales(List<String> l)  { this.galeriaImagenes = l; }

    /** Anadir imagen a la galeria (usado en ProductoController) */
    public void addImagenGaleria(String url) {
        if (galeriaImagenes == null) galeriaImagenes = new ArrayList<>();
        galeriaImagenes.add(url);
    }
}