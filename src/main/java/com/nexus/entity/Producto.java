package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto extends DomainEntity {

    @NotBlank
    private String titulo;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Min(0)
    private double precio;
    
    // ✅ NUEVO: Imagen principal OBLIGATORIA
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String imagenPrincipal;
    
    // ✅ NUEVO: Galería de imágenes OPCIONAL (máximo 5 fotos extra)
    @ElementCollection
    @CollectionTable(name = "producto_galeria", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private List<String> galeriaImagenes = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoOferta tipoOferta;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoProducto estadoProducto;

    @ManyToOne
    @JoinColumn(name = "publicador_id")
    private Usuario publicador;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Mensaje> mensajes = new ArrayList<>();

    public Producto() {
        super();
        this.estadoProducto = EstadoProducto.DISPONIBLE; 
    }

    // CONSTRUCTOR COMPLETO
    public Producto(String titulo, String descripcion, double precio, TipoOferta tipoOferta, Usuario publicador, String imagenPrincipal) {
        super();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipoOferta = tipoOferta;
        this.publicador = publicador;
        this.imagenPrincipal = imagenPrincipal;
        this.estadoProducto = EstadoProducto.DISPONIBLE;
    }

    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        mensaje.setProducto(this);
    }

    public void removeMensaje(Mensaje mensaje) {
        mensajes.remove(mensaje);
        mensaje.setProducto(null);
    }

    // ✅ NUEVO: Método para añadir imágenes a la galería con validación
    public void addImagenGaleria(String url) {
        if (galeriaImagenes.size() < 5) { // Máximo 5 imágenes extra
            galeriaImagenes.add(url);
        } else {
            throw new IllegalStateException("Máximo 5 imágenes en la galería");
        }
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }

    public List<String> getGaleriaImagenes() { return galeriaImagenes; }
    public void setGaleriaImagenes(List<String> galeriaImagenes) { this.galeriaImagenes = galeriaImagenes; }

    public TipoOferta getTipoOferta() { return tipoOferta; }
    public void setTipoOferta(TipoOferta tipoOferta) { this.tipoOferta = tipoOferta; }

    public EstadoProducto getEstadoProducto() { return estadoProducto; }
    public void setEstadoProducto(EstadoProducto estadoProducto) { this.estadoProducto = estadoProducto; }

    public Usuario getPublicador() { return publicador; }
    public void setPublicador(Usuario publicador) { this.publicador = publicador; }

    public List<Mensaje> getMensajes() { return mensajes; }
    public void setMensajes(List<Mensaje> mensajes) { this.mensajes = mensajes; }
}