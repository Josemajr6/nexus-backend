package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reseña de 5 estrellas que deja el comprador al vendedor tras una compra completada.
 *
 * Una compra solo puede generar UNA valoración (constraint unique).
 * La reputación del vendedor se calcula como la media de todas sus valoraciones.
 */
@Entity
@Table(name = "valoracion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"compra_id"}))
public class Valoracion extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Actor vendedor;

    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false, unique = true)
    private Compra compra;

    /** 1–5 estrellas */
    @Column(nullable = false)
    private Integer estrellas;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    /** Respuesta pública del vendedor a la reseña */
    @Column(columnDefinition = "TEXT")
    private String respuestaVendedor;

    private LocalDateTime fechaValoracion;
    private LocalDateTime fechaRespuesta;

    public Valoracion() { super(); this.fechaValoracion = LocalDateTime.now(); }

    public Usuario  getComprador()              { return comprador; }
    public void     setComprador(Usuario c)     { this.comprador = c; }
    public Actor    getVendedor()               { return vendedor; }
    public void     setVendedor(Actor v)        { this.vendedor = v; }
    public Compra   getCompra()                 { return compra; }
    public void     setCompra(Compra c)         { this.compra = c; }
    public Integer  getEstrellas()              { return estrellas; }
    public void     setEstrellas(Integer e)     { this.estrellas = e; }
    public String   getComentario()             { return comentario; }
    public void     setComentario(String c)     { this.comentario = c; }
    public String   getRespuestaVendedor()      { return respuestaVendedor; }
    public void     setRespuestaVendedor(String r) { this.respuestaVendedor = r; }
    public LocalDateTime getFechaValoracion()   { return fechaValoracion; }
    public LocalDateTime getFechaRespuesta()    { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime f) { this.fechaRespuesta = f; }
}