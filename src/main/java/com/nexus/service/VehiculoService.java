package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.*;
import com.nexus.repository.*;

@Service
public class VehiculoService {

    @Autowired private VehiculoRepository  vehiculoRepository;
    @Autowired private ActorRepository     actorRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private StorageService      storageService;

    // ---- CRUD basico ----------------------------------------------------

    public List<Vehiculo> findAll() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> findById(Integer id) {
        return vehiculoRepository.findById(id);
    }

    @Transactional
    public Vehiculo save(Vehiculo vehiculo) {
        asegurarCategoriaVehiculos(vehiculo);
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void deleteById(Integer id) {
        vehiculoRepository.findById(id).ifPresent(v -> {
            v.setEstadoVehiculo(EstadoVehiculo.ELIMINADO);
            vehiculoRepository.save(v);
        });
    }

    // ---- Crear con imagenes --------------------------------------------

    @Transactional
    public Vehiculo crear(Vehiculo vehiculo, Integer publicadorId,
                          List<MultipartFile> imagenes) {
        Actor publicador = actorRepository.findById(publicadorId)
            .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado"));
        vehiculo.setPublicador(publicador);
        vehiculo.setFechaPublicacion(LocalDateTime.now());
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        asegurarCategoriaVehiculos(vehiculo);

        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                String url = storageService.subirImagen(img);
                if (url != null) {
                    if (vehiculo.getImagenPrincipal() == null) vehiculo.setImagenPrincipal(url);
                    else vehiculo.addImagenGaleria(url);
                }
            }
        }
        return vehiculoRepository.save(vehiculo);
    }

    // ---- Actualizar (PATCH) --------------------------------------------

    @Transactional
    public Vehiculo actualizar(Integer id, Vehiculo datos) {
        Vehiculo v = vehiculoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado: " + id));

        if (datos.getTitulo()        != null) v.setTitulo(datos.getTitulo());
        if (datos.getDescripcion()   != null) v.setDescripcion(datos.getDescripcion());
        if (datos.getPrecio()        != null) v.setPrecio(datos.getPrecio());
        if (datos.getMarca()         != null) v.setMarca(datos.getMarca());
        if (datos.getModelo()        != null) v.setModelo(datos.getModelo());
        if (datos.getAnio()          != null) v.setAnio(datos.getAnio());
        if (datos.getKilometros()    != null) v.setKilometros(datos.getKilometros());
        if (datos.getCombustible()   != null) v.setCombustible(datos.getCombustible());
        if (datos.getCambio()        != null) v.setCambio(datos.getCambio());
        if (datos.getPotencia()      != null) v.setPotencia(datos.getPotencia());
        if (datos.getCilindrada()    != null) v.setCilindrada(datos.getCilindrada());
        if (datos.getColor()         != null) v.setColor(datos.getColor());
        if (datos.getNumeroPuertas() != null) v.setNumeroPuertas(datos.getNumeroPuertas());
        if (datos.getPlazas()        != null) v.setPlazas(datos.getPlazas());
        if (datos.getUbicacion()     != null) v.setUbicacion(datos.getUbicacion());
        if (datos.getTipoVehiculo()  != null) v.setTipoVehiculo(datos.getTipoVehiculo());
        if (datos.getEstadoVehiculo()!= null) v.setEstadoVehiculo(datos.getEstadoVehiculo());
        if (datos.getTipoOferta()    != null) v.setTipoOferta(datos.getTipoOferta());
        if (datos.getCondicion()     != null) v.setCondicion(datos.getCondicion());
        if (datos.getItv()           != null) v.setItv(datos.getItv());
        if (datos.getGarantia()      != null) v.setGarantia(datos.getGarantia());

        return vehiculoRepository.save(v);
    }

    // ---- Busqueda con filtros ------------------------------------------

    public Page<Vehiculo> buscarConFiltros(String tipo, String marca,
                                            Integer anioMin, Integer anioMax,
                                            Integer kmMax, Double precioMin,
                                            Double precioMax, String combustible,
                                            String cambio, Pageable pageable) {
        return vehiculoRepository.buscarConFiltros(
            tipo, marca, anioMin, anioMax, kmMax,
            precioMin, precioMax, combustible, cambio, pageable);
    }

    public List<Vehiculo> getByPublicadorId(Integer publicadorId) {
        return vehiculoRepository.findByPublicadorIdOrderByFechaPublicacionDesc(publicadorId);
    }

    // ---- Helper: asignar categoria "Vehiculos" automaticamente ----------

    private void asegurarCategoriaVehiculos(Vehiculo vehiculo) {
        if (vehiculo.getCategoria() != null) return;
        Categoria cat = categoriaRepository.findBySlug("vehiculos")
            .orElseGet(() -> {
                // Crear la categoria si no existe
                Categoria nueva = new Categoria("Vehiculos", "vehiculos", "directions_car");
                nueva.setColor("#1976D2");
                nueva.setOrden(5);
                nueva.setActiva(true);
                return categoriaRepository.save(nueva);
            });
        vehiculo.setCategoria(cat);
    }
}