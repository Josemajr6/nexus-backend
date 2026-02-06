package com.nexus.entity.vehiculos;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nexus.entity.Producto;

@Entity
public class Vehiculo extends Producto {
    
    @Enumerated(EnumType.STRING)
    private TipoVehiculo tipoVehiculo; // COCHE, MOTO, FURGONETA, CARAVANA
    
    private String marca;
    private String modelo;
    private Integer anio;
    private Integer kilometros;
    
    @Enumerated(EnumType.STRING)
    private TipoCombustible combustible; // GASOLINA, DIESEL, ELECTRICO, HIBRIDO, GLP
    
    private Integer cilindrada; // cc
    private Integer potencia; // CV
    
    @Enumerated(EnumType.STRING)
    private TipoCambio cambio; // MANUAL, AUTOMATICO
    
    private Integer numPuertas;
    private Integer numPlazas;
    private String color;
    
    @ElementCollection
    @CollectionTable(name = "vehiculo_extras", joinColumns = @JoinColumn(name = "vehiculo_id"))
    @Column(name = "extra")
    private List<String> extras = new ArrayList<>(); // ["GPS", "Aire acondicionado", "Llantas de aleación"]
    
    @Column(columnDefinition = "TEXT")
    private String matricula;
    
    private Boolean esSegundaMano;
    private Boolean tieneITV;
    private LocalDateTime fechaProximaITV;
    
    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estadoVehiculo; // EXCELENTE, MUY_BUENO, BUENO, ACEPTABLE, NECESITA_REPARACION
    
    public Vehiculo() {
        super();
        this.extras = new ArrayList<>();
    }
    
    // Getters y Setters
    public TipoVehiculo getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    
    public Integer getKilometros() { return kilometros; }
    public void setKilometros(Integer kilometros) { this.kilometros = kilometros; }
    
    public TipoCombustible getCombustible() { return combustible; }
    public void setCombustible(TipoCombustible combustible) { this.combustible = combustible; }
    
    public Integer getCilindrada() { return cilindrada; }
    public void setCilindrada(Integer cilindrada) { this.cilindrada = cilindrada; }
    
    public Integer getPotencia() { return potencia; }
    public void setPotencia(Integer potencia) { this.potencia = potencia; }
    
    public TipoCambio getCambio() { return cambio; }
    public void setCambio(TipoCambio cambio) { this.cambio = cambio; }
    
    public Integer getNumPuertas() { return numPuertas; }
    public void setNumPuertas(Integer numPuertas) { this.numPuertas = numPuertas; }
    
    public Integer getNumPlazas() { return numPlazas; }
    public void setNumPlazas(Integer numPlazas) { this.numPlazas = numPlazas; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public List<String> getExtras() { return extras; }
    public void setExtras(List<String> extras) { this.extras = extras; }
    
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    
    public Boolean getEsSegundaMano() { return esSegundaMano; }
    public void setEsSegundaMano(Boolean esSegundaMano) { this.esSegundaMano = esSegundaMano; }
    
    public Boolean getTieneITV() { return tieneITV; }
    public void setTieneITV(Boolean tieneITV) { this.tieneITV = tieneITV; }
    
    public LocalDateTime getFechaProximaITV() { return fechaProximaITV; }
    public void setFechaProximaITV(LocalDateTime fechaProximaITV) { this.fechaProximaITV = fechaProximaITV; }
    
    public EstadoVehiculo getEstadoVehiculo() { return estadoVehiculo; }
    public void setEstadoVehiculo(EstadoVehiculo estadoVehiculo) { this.estadoVehiculo = estadoVehiculo; }
}