package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.*;
import com.nexus.repository.*;

/**
 * VehiculoService -- todos los metodos que usa VehiculoController:
 *   findDisponibles(), findByTipo(), getMarcasDisponibles(),
 *   getVehiculosDeUsuario(), publicar(), update(), delete()
 */
@Service
public class VehiculoService {

    @Autowired private VehiculoRepository  vehiculoRepository;
    @Autowired private ActorRepository     actorRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private StorageService      storageService;

    // ---- CRUD basico ---------------------------------------------------

    public List<Vehiculo>    findAll()                   { return vehiculoRepository.findAll(); }
    public Optional<Vehiculo> findById(Integer id)       { return vehiculoRepository.findById(id); }

    @Transactional
    public Vehiculo save(Vehiculo v) {
        asegurarCategoria(v);
        return vehiculoRepository.save(v);
    }

    // ---- Listados especificos (VehiculoController) ----------------------

    /** findDisponibles() -- VehiculoController line 33 */
    public List<Vehiculo> findDisponibles() {
        return vehiculoRepository.findByEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
    }

    /** findByTipo(TipoVehiculo) -- VehiculoController line 35 */
    public List<Vehiculo> findByTipo(TipoVehiculo tipo) {
        return vehiculoRepository.findByTipoVehiculoAndEstadoVehiculo(tipo, EstadoVehiculo.DISPONIBLE);
    }

    /** getMarcasDisponibles() -- VehiculoController line 37 */
    public List<String> getMarcasDisponibles() {
        return vehiculoRepository.findMarcasDistintas();
    }

    /** getVehiculosDeUsuario(Integer) -- VehiculoController line 39 */
    public List<Vehiculo> getVehiculosDeUsuario(Integer publicadorId) {
        return vehiculoRepository.findByPublicadorIdOrderByFechaPublicacionDesc(publicadorId);
    }

    // ---- Crear / Publicar ----------------------------------------------

    /**
     * publicar(Vehiculo, Integer publicadorId) -- VehiculoController line 80.
     * Asigna publicador, fecha y categoria automatica.
     */
    @Transactional
    public Vehiculo publicar(Vehiculo vehiculo, Integer publicadorId) {
        Actor publicador = actorRepository.findById(publicadorId)
            .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado: " + publicadorId));
        vehiculo.setPublicador(publicador);
        vehiculo.setFechaPublicacion(LocalDateTime.now());
        if (vehiculo.getEstadoVehiculo() == null) vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        asegurarCategoria(vehiculo);
        return vehiculoRepository.save(vehiculo);
    }

    /** Crear con imagenes adjuntas en multipart. */
    @Transactional
    public Vehiculo crear(Vehiculo vehiculo, Integer publicadorId, List<MultipartFile> imagenes) {
        Actor publicador = actorRepository.findById(publicadorId)
            .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado: " + publicadorId));
        vehiculo.setPublicador(publicador);
        vehiculo.setFechaPublicacion(LocalDateTime.now());
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        asegurarCategoria(vehiculo);

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

    // ---- Actualizar ---------------------------------------------------

    /**
     * update(Integer id, Vehiculo datos) -- VehiculoController line 101 (PATCH).
     */
    @Transactional
    public Vehiculo update(Integer id, Vehiculo datos) {
        Vehiculo v = vehiculoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado: " + id));

        if (datos.getTitulo()         != null) v.setTitulo(datos.getTitulo());
        if (datos.getDescripcion()    != null) v.setDescripcion(datos.getDescripcion());
        if (datos.getPrecio()         != null) v.setPrecio(datos.getPrecio());
        if (datos.getMarca()          != null) v.setMarca(datos.getMarca());
        if (datos.getModelo()         != null) v.setModelo(datos.getModelo());
        if (datos.getAnio()           != null) v.setAnio(datos.getAnio());
        if (datos.getKilometros()     != null) v.setKilometros(datos.getKilometros());
        if (datos.getCombustible()    != null) v.setCombustible(datos.getCombustible());
        if (datos.getCambio()         != null) v.setCambio(datos.getCambio());
        if (datos.getPotencia()       != null) v.setPotencia(datos.getPotencia());
        if (datos.getCilindrada()     != null) v.setCilindrada(datos.getCilindrada());
        if (datos.getColor()          != null) v.setColor(datos.getColor());
        if (datos.getNumeroPuertas()  != null) v.setNumeroPuertas(datos.getNumeroPuertas());
        if (datos.getPlazas()         != null) v.setPlazas(datos.getPlazas());
        if (datos.getUbicacion()      != null) v.setUbicacion(datos.getUbicacion());
        if (datos.getTipoVehiculo()   != null) v.setTipoVehiculo(datos.getTipoVehiculo());
        if (datos.getEstadoVehiculo() != null) v.setEstadoVehiculo(datos.getEstadoVehiculo());
        if (datos.getTipoOferta()     != null) v.setTipoOferta(datos.getTipoOferta());
        if (datos.getCondicion()      != null) v.setCondicion(datos.getCondicion());
        if (datos.getItv()            != null) v.setItv(datos.getItv());
        if (datos.getGarantia()       != null) v.setGarantia(datos.getGarantia());

        return vehiculoRepository.save(v);
    }

    // ---- Eliminar (soft-delete) ----------------------------------------

    /**
     * delete(Integer id) -- VehiculoController line 110.
     */
    @Transactional
    public void delete(Integer id) {
        vehiculoRepository.findById(id).ifPresent(v -> {
            v.setEstadoVehiculo(EstadoVehiculo.ELIMINADO);
            vehiculoRepository.save(v);
        });
    }

    /** Alias para deleteById */
    @Transactional
    public void deleteById(Integer id) { delete(id); }

    // ---- Busqueda con filtros -----------------------------------------

    public Page<Vehiculo> buscarConFiltros(String tipo, String marca,
                                            Integer anioMin, Integer anioMax,
                                            Integer kmMax, Double precioMin,
                                            Double precioMax, String combustible,
                                            String cambio, Pageable pageable) {
        return vehiculoRepository.buscarConFiltros(
            tipo, marca, anioMin, anioMax, kmMax,
            precioMin, precioMax, combustible, cambio, pageable);
    }

    // ---- Helper ---------------------------------------------------------

    private void asegurarCategoria(Vehiculo v) {
        if (v.getCategoria() != null) return;
        Categoria cat = categoriaRepository.findBySlug("vehiculos").orElseGet(() -> {
            Categoria nueva = new Categoria("Vehiculos", "vehiculos", "directions_car");
            nueva.setColor("#1976D2");
            nueva.setOrden(5);
            nueva.setActiva(true);
            return categoriaRepository.save(nueva);
        });
        v.setCategoria(cat);
    }
}