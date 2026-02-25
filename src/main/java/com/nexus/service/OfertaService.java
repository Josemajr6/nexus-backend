package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.*;
import com.nexus.repository.*;

@Service
public class OfertaService {

    @Autowired private OfertaRepository    ofertaRepository;
    @Autowired private ActorRepository     actorRepository;
    @Autowired private SparkVotoRepository sparkVotoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private StorageService      storageService;
    @Autowired private NotificacionService notificacionService;

    // ---- CRUD ----------------------------------------------------------

    public List<Oferta>    findAll()                   { return ofertaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPublicacion")); }
    public Optional<Oferta> findById(Integer id)       { return ofertaRepository.findById(id); }
    public Oferta findByIdOrThrow(Integer id)          { return ofertaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada: " + id)); }

    @Transactional
    public Oferta save(Oferta oferta) {
        if (oferta.getFechaPublicacion() == null) oferta.setFechaPublicacion(LocalDateTime.now());
        oferta.actualizarBadge();
        return ofertaRepository.save(oferta);
    }

    @Transactional
    public void deleteById(Integer id) { ofertaRepository.deleteById(id); }

    // ---- Crear con imagenes y categoria --------------------------------

    @Transactional
    public Oferta crear(Oferta oferta, Integer actorId, List<MultipartFile> imagenes) {
        Actor actor = actorRepository.findById(actorId)
            .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado"));
        oferta.setActor(actor);
        oferta.setFechaPublicacion(LocalDateTime.now());
        oferta.setEsActiva(true);
        if (oferta.getSparkCount() == null) oferta.setSparkCount(0);
        if (oferta.getDripCount()  == null) oferta.setDripCount(0);

        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                String url = storageService.subirImagen(img);
                if (url != null) {
                    if (oferta.getImagenPrincipal() == null) oferta.setImagenPrincipal(url);
                    else oferta.addImagenGaleria(url);
                }
            }
        }
        oferta.actualizarBadge();
        Oferta guardada = ofertaRepository.save(oferta);
        notificacionService.notificarSparkEnOferta(actorId, guardada.getTitulo());
        return guardada;
    }

    /**
     * Resolver categoria por nombre y asignarla.
     * Usar esto en lugar de oferta.setCategoria("Electronica")
     */
    @Transactional
    public void setCategoriaByNombre(Oferta oferta, String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        categoriaRepository.findByNombre(nombre)
            .or(() -> categoriaRepository.findBySlug(
                nombre.toLowerCase().replaceAll("[^a-z0-9]", "-")))
            .ifPresent(oferta::setCategoria);
    }

    // ---- Listados especiales -------------------------------------------

    public List<Oferta> obtenerDestacadas()       { return ofertaRepository.findDestacadas(LocalDateTime.now().minusDays(7), PageRequest.of(0, 20)); }
    public List<Oferta> obtenerTrending()         { return ofertaRepository.findTrending(LocalDateTime.now().minusHours(24), PageRequest.of(0, 20)); }
    public List<Oferta> obtenerTopSpark()         { return ofertaRepository.findTopBySparkScore(PageRequest.of(0, 20)); }
    public List<Oferta> obtenerProximasExpirar()  { return ofertaRepository.findProximasExpirar(LocalDateTime.now(), LocalDateTime.now().plusHours(24)); }
    public List<Oferta> getRecientes(int limite)  { return ofertaRepository.findRecientes(PageRequest.of(0, limite)); }
    public List<Oferta> getByCategoria(String c)  { return ofertaRepository.findByCategoria(c); }
    public List<Oferta> getByBadge(BadgeOferta b) { return ofertaRepository.findByBadgeAndEsActivaTrue(b); }
    public List<Oferta> buscarTexto(String q)     { return ofertaRepository.buscarPorTexto(q); }
    public List<Oferta> getByActorId(Integer id)  { return ofertaRepository.findByActorId(id); }

    // ---- Busqueda con filtros ------------------------------------------

    public Page<Oferta> buscarConFiltros(String categoria, String tienda,
                                          Double precioMin, Double precioMax,
                                          String busqueda, Boolean soloActivas,
                                          String sortField, String sortDir,
                                          Pageable pageable) {
        boolean solo = Boolean.TRUE.equals(soloActivas);
        if (!pageable.getSort().isSorted() && sortField != null && !sortField.isBlank()) {
            Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(Sort.Direction.ASC,  sortField)
                : Sort.by(Sort.Direction.DESC, sortField);
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }
        return ofertaRepository.buscarConFiltros(
            categoria, tienda, precioMin, precioMax, busqueda, solo, pageable);
    }

    public Page<Oferta> buscarConFiltros(String categoria, String tienda,
                                          Double precioMin, Double precioMax,
                                          String busqueda, boolean soloActivas,
                                          int page, int size) {
        return ofertaRepository.buscarConFiltros(
            categoria, tienda, precioMin, precioMax, busqueda, soloActivas,
            PageRequest.of(page, size));
    }

    // ---- Interacciones -------------------------------------------------

    @Transactional
    public void incrementarVistas(Integer id) {
        ofertaRepository.findById(id).ifPresent(o -> {
            o.setNumeroVistas(o.getNumeroVistas() != null ? o.getNumeroVistas() + 1 : 1);
            ofertaRepository.save(o);
        });
    }

    @Transactional
    public void incrementarCompartidos(Integer id) {
        ofertaRepository.findById(id).ifPresent(o -> {
            o.setNumeroCompartidos(o.getNumeroCompartidos() != null ? o.getNumeroCompartidos() + 1 : 1);
            ofertaRepository.save(o);
        });
    }

    // ---- Votos ---------------------------------------------------------

    @Transactional
    public int votarOferta(Integer actorId, Integer ofertaId, Boolean isUpvote) {
        int valor = Boolean.TRUE.equals(isUpvote) ? 1 : -1;
        Oferta oferta = findByIdOrThrow(ofertaId);
        Actor  actor  = actorRepository.findById(actorId)
            .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado"));

        Optional<SparkVoto> prev = sparkVotoRepository.findByActorIdAndOfertaId(actorId, ofertaId);
        if (prev.isPresent()) {
            SparkVoto v = prev.get();
            if (v.getValor() == valor) {
                if (valor == 1) oferta.setSparkCount(Math.max(0, oferta.getSparkCount() - 1));
                else            oferta.setDripCount(Math.max(0,  oferta.getDripCount()  - 1));
                sparkVotoRepository.deleteByActorAndOferta(actorId, ofertaId);
            } else {
                if (valor == 1) { oferta.setSparkCount(oferta.getSparkCount() + 1); oferta.setDripCount(Math.max(0, oferta.getDripCount() - 1)); }
                else            { oferta.setDripCount(oferta.getDripCount() + 1);   oferta.setSparkCount(Math.max(0, oferta.getSparkCount() - 1)); }
                v.setValor(valor);
                sparkVotoRepository.save(v);
            }
        } else {
            sparkVotoRepository.save(new SparkVoto(actor, oferta, Boolean.TRUE.equals(isUpvote)));
            if (valor == 1) oferta.setSparkCount(oferta.getSparkCount() + 1);
            else            oferta.setDripCount(oferta.getDripCount() + 1);
        }
        ofertaRepository.save(oferta);
        return oferta.getSparkScore();
    }

    // ---- Meta-datos -------------------------------------------------------

    public List<String>        getCategorias()   { return ofertaRepository.findCategoriasDistintas(); }
    public List<String>        getTiendas()      { return ofertaRepository.findTiendasDistintas(); }
    public Map<String, Object> getEstadisticas() {
        return Map.of("totalActivas", ofertaRepository.countActivas(),
                      "categorias",  getCategorias(), "tiendas", getTiendas());
    }
}