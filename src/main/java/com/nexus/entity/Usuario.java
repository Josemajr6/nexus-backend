package com.nexus.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Usuario extends Actor {

    private String telefono;
    
    private boolean esVerificado; 
    
    // ✅ NUEVO: Avatar con valor por defecto
    @Column(columnDefinition = "TEXT")
    private String avatar = "https://res.cloudinary.com/dzahpgslo/image/upload/v1234567890/defaults/avatar-default.png";
    
    private String biografia;
    private Integer reputacion;
    private String ubicacion;

    @OneToMany(mappedBy = "publicador", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "comprador", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Compra> compras = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Mensaje> mensajes = new ArrayList<>();

    public Usuario() {
        super();
        this.esVerificado = false;
        this.reputacion = 0;
    }

    // Getters y Setters
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isEsVerificado() { return esVerificado; }
    public void setEsVerificado(boolean esVerificado) { this.esVerificado = esVerificado; }

    // ✅ NUEVO: Getters/Setters para avatar
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar != null ? avatar : "https://res.cloudinary.com/dzahpgslo/image/upload/v1234567890/defaults/avatar-default.png"; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public Integer getReputacion() { return reputacion; }
    public void setReputacion(Integer reputacion) { this.reputacion = reputacion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public List<Compra> getCompras() { return compras; }
    public void setCompras(List<Compra> compras) { this.compras = compras; }

    public List<Mensaje> getMensajes() { return mensajes; }
    public void setMensajes(List<Mensaje> mensajes) { this.mensajes = mensajes; }
}