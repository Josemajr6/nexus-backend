package com.nexus.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nexus.entity.EstadoProducto;
import com.nexus.entity.Usuario;
import com.nexus.entity.vehiculos.*;
import com.nexus.repository.VehiculoRepository;

@Service
public class VehiculoService {

    @Autowired private VehiculoRepository vehiculoRepository;
    @Autowired private UsuarioService usuarioService;

    public List<Vehiculo> findAll()                    { return vehiculoRepository.findAll(); }
    public Optional<Vehiculo> findById(Integer id)     { return vehiculoRepository.findById(id); }
    public List<Vehiculo> findDisponibles()            { return vehiculoRepository.findByEstadoProducto(EstadoProducto.DISPONIBLE); }
    public List<Vehiculo> findByTipo(TipoVehiculo t)   { return vehiculoRepository.findByTipoVehiculo(t); }
    public List<String> getMarcasDisponibles()         { return vehiculoRepository.findMarcasDisponibles(); }
    public List<Vehiculo> getVehiculosDeUsuario(Integer id) { return vehiculoRepository.findByPublicadorId(id); }

    public Page<Vehiculo> buscarPaginado(TipoVehiculo tipo, String marca, String modelo,
            Double precioMin, Double precioMax, Integer anioMin, Integer kmMax,
            TipoCombustible combustible, Pageable pageable) {

        List<Vehiculo> lista = vehiculoRepository.buscarConFiltros(
            tipo, marca, modelo, precioMin, precioMax, anioMin, kmMax, combustible);
        int inicio = (int) pageable.getOffset();
        int fin    = Math.min(inicio + pageable.getPageSize(), lista.size());
        if (inicio > lista.size()) return new PageImpl<>(List.of(), pageable, lista.size());
        return new PageImpl<>(lista.subList(inicio, fin), pageable, lista.size());
    }

    public Vehiculo publicar(Vehiculo v, Integer usuarioId) {
        return usuarioService.findById(usuarioId).map(u -> {
            v.setPublicador(u); v.setEstadoProducto(EstadoProducto.DISPONIBLE);
            return vehiculoRepository.save(v);
        }).orElse(null);
    }

    public Vehiculo update(Integer id, Vehiculo d) {
        return vehiculoRepository.findById(id).map(v -> {
            if (d.getTitulo()        != null) v.setTitulo(d.getTitulo());
            if (d.getDescripcion()   != null) v.setDescripcion(d.getDescripcion());
            if (d.getPrecio()        >  0)    v.setPrecio(d.getPrecio());
            if (d.getTipoOferta()    != null) v.setTipoOferta(d.getTipoOferta());
            if (d.getEstadoProducto()!= null) v.setEstadoProducto(d.getEstadoProducto());
            if (d.getMarca()         != null) v.setMarca(d.getMarca());
            if (d.getModelo()        != null) v.setModelo(d.getModelo());
            if (d.getAnio()          != null) v.setAnio(d.getAnio());
            if (d.getKilometros()    != null) v.setKilometros(d.getKilometros());
            if (d.getCombustible()   != null) v.setCombustible(d.getCombustible());
            if (d.getCilindrada()    != null) v.setCilindrada(d.getCilindrada());
            if (d.getPotencia()      != null) v.setPotencia(d.getPotencia());
            if (d.getCambio()        != null) v.setCambio(d.getCambio());
            if (d.getColor()         != null) v.setColor(d.getColor());
            if (d.getTipoVehiculo()  != null) v.setTipoVehiculo(d.getTipoVehiculo());
            if (d.getEstadoVehiculo()!= null) v.setEstadoVehiculo(d.getEstadoVehiculo());
            if (d.getImagenPrincipal()!=null) v.setImagenPrincipal(d.getImagenPrincipal());
            return vehiculoRepository.save(v);
        }).orElse(null);
    }

    public void delete(Integer id) { if (vehiculoRepository.existsById(id)) vehiculoRepository.deleteById(id); }
}