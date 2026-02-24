package com.nexus.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Categoria de Productos y Ofertas.
 * Permite filtrar en Angular por categoria.
 *
 * Ejemplos raiz: Electronica, Ropa, Vehiculos, Hogar, Deportes
 * Sub-categorias: Moviles (hijo de Electronica), Coches (hijo de Vehiculos)
 */
@Entity
@Table(name = "categoria")
public class Categoria extends DomainEntity {

    @Column(nullable = false, unique = true)
    private String nombre;

    /** Slug para URLs: electronica, ropa-hombre, coches */
    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Nombre de icono Material (smartphone, directions_car...) o URL de imagen */
    private String icono;

    /** Color de acento en HEX (#FF5722) para Angular */
    private String color;

    /** Orden de aparicion en el menu */
    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false)
    private Boolean activa = true;

    /** Categoria padre (null si es categoria raiz) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Categoria parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Categoria> hijos = new ArrayList<>();

    public Categoria() {}

    public Categoria(String nombre, String slug, String icono) {
        this.nombre = nombre;
        this.slug   = slug;
        this.icono  = icono;
    }

    public String    getNombre()                   { return nombre; }
    public void      setNombre(String n)           { this.nombre = n; }
    public String    getSlug()                     { return slug; }
    public void      setSlug(String s)             { this.slug = s; }
    public String    getDescripcion()              { return descripcion; }
    public void      setDescripcion(String d)      { this.descripcion = d; }
    public String    getIcono()                    { return icono; }
    public void      setIcono(String i)            { this.icono = i; }
    public String    getColor()                    { return color; }
    public void      setColor(String c)            { this.color = c; }
    public Integer   getOrden()                    { return orden; }
    public void      setOrden(Integer o)           { this.orden = o; }
    public Boolean   getActiva()                   { return activa; }
    public void      setActiva(Boolean a)          { this.activa = a; }
    public Categoria getParent()                   { return parent; }
    public void      setParent(Categoria p)        { this.parent = p; }
    public List<Categoria> getHijos()              { return hijos; }
    public void      setHijos(List<Categoria> h)   { this.hijos = h; }
}