package com.nexus.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Empresa extends Actor {

    private String cif;

    // RELACIÓN: Empresa tiene 0..* Contratos
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Contrato> contratos = new ArrayList<>();

    public Empresa() {
        super();
    }

    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    public List<Contrato> getContratos() { return contratos; }
    public void setContratos(List<Contrato> contratos) { this.contratos = contratos; }
}