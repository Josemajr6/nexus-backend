package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.Oferta;
import com.nexus.entity.SparkVoto;
import com.nexus.entity.Usuario;
import com.nexus.repository.OfertaRepository;
import com.nexus.repository.SparkVotoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class OfertaService {
    
    @Autowired
    private OfertaRepository ofertaRepository;
    
    @Autowired
    private SparkVotoRepository sparkVotoRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private EntityManager entityManager;
    
    // Búsqueda dinámica con Criteria API
    public Page<Oferta> buscarConFiltros(
            String categoria,
            String tienda,
            Double precioMin,
            Double precioMax,
            String busqueda,
            Boolean soloActivas,
            String ordenarPor,
            String direccion,
            Pageable pageable) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Oferta> cq = cb.createQuery(Oferta.class);
        Root<Oferta> oferta = cq.from(Oferta.class);
        
        // Construir predicados dinámicamente
        List<Predicate> predicates = construirPredicados(cb, oferta, categoria, tienda, 
                                                         precioMin, precioMax, busqueda, soloActivas);
        
        cq.where(predicates.toArray(new Predicate[0]));
        
        // Ordenamiento
        aplicarOrdenamiento(cb, cq, oferta, ordenarPor, direccion);
        
        // Ejecutar query
        TypedQuery<Oferta> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Oferta> resultados = query.getResultList();
        
        // Contar total
        long total = contarResultados(categoria, tienda, precioMin, precioMax, busqueda, soloActivas);
        
        return new PageImpl<>(resultados, pageable, total);
    }
    
    private List<Predicate> construirPredicados(CriteriaBuilder cb, Root<Oferta> root,
                                                String categoria, String tienda,
                                                Double precioMin, Double precioMax,
                                                String busqueda, Boolean soloActivas) {
        List<Predicate> predicates = new java.util.ArrayList<>();
        
        if (categoria != null && !categoria.isEmpty()) {
            predicates.add(cb.equal(root.get("categoria"), categoria));
        }
        
        if (tienda != null && !tienda.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("tienda")), "%" + tienda.toLowerCase() + "%"));
        }
        
        if (precioMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("precioOferta"), precioMin));
        }
        
        if (precioMax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("precioOferta"), precioMax));
        }
        
        if (busqueda != null && !busqueda.isEmpty()) {
            String searchPattern = "%" + busqueda.toLowerCase() + "%";
            Predicate tituloMatch = cb.like(cb.lower(root.get("titulo")), searchPattern);
            Predicate descripcionMatch = cb.like(cb.lower(root.get("descripcion")), searchPattern);
            predicates.add(cb.or(tituloMatch, descripcionMatch));
        }
        
        if (soloActivas != null && soloActivas) {
            predicates.add(cb.equal(root.get("esActiva"), true));
        }
        
        return predicates;
    }
    
    private void aplicarOrdenamiento(CriteriaBuilder cb, CriteriaQuery<Oferta> cq, 
                                    Root<Oferta> root, String ordenarPor, String direccion) {
        boolean asc = "asc".equalsIgnoreCase(direccion);
        
        switch (ordenarPor != null ? ordenarPor : "fecha") {
            case "precio":
                cq.orderBy(asc ? cb.asc(root.get("precioOferta")) : cb.desc(root.get("precioOferta")));
                break;
            case "spark":
                cq.orderBy(asc ? cb.asc(root.get("sparkCount")) : cb.desc(root.get("sparkCount")));
                break;
            case "vistas":
                cq.orderBy(asc ? cb.asc(root.get("numeroVistas")) : cb.desc(root.get("numeroVistas")));
                break;
            default:
                cq.orderBy(asc ? cb.asc(root.get("fechaPublicacion")) : cb.desc(root.get("fechaPublicacion")));
        }
    }
    
    private long contarResultados(String categoria, String tienda, Double precioMin, 
                                 Double precioMax, String busqueda, Boolean soloActivas) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Oferta> root = cq.from(Oferta.class);
        
        List<Predicate> predicates = construirPredicados(cb, root, categoria, tienda, 
                                                        precioMin, precioMax, busqueda, soloActivas);
        
        cq.select(cb.count(root));
        cq.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(cq).getSingleResult();
    }
    
    // ⚡ SISTEMA SPARK
    @Transactional
    public void votarOferta(Integer ofertaId, Integer usuarioId, Boolean esSpark) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(ofertaId);
        if (ofertaOpt.isEmpty()) {
            throw new IllegalArgumentException("Oferta no encontrada");
        }
        
        Oferta oferta = ofertaOpt.get();
        Optional<SparkVoto> votoExistente = sparkVotoRepository.findByUsuarioAndOferta(usuarioId, ofertaId);
        
        if (votoExistente.isPresent()) {
            SparkVoto voto = votoExistente.get();
            
            if (voto.getEsSpark().equals(esSpark)) {
                // Quitar voto
                if (esSpark) oferta.decrementarSpark();
                else oferta.decrementarDrip();
                sparkVotoRepository.delete(voto);
            } else {
                // Cambiar voto
                if (esSpark) {
                    oferta.decrementarDrip();
                    oferta.incrementarSpark();
                } else {
                    oferta.decrementarSpark();
                    oferta.incrementarDrip();
                }
                voto.setEsSpark(esSpark);
                voto.setFechaVoto(LocalDateTime.now());
                sparkVotoRepository.save(voto);
            }
        } else {
            // Nuevo voto
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            
            SparkVoto nuevoVoto = new SparkVoto(usuario, oferta, esSpark);
            sparkVotoRepository.save(nuevoVoto);
            
            if (esSpark) oferta.incrementarSpark();
            else oferta.incrementarDrip();
        }
        
        ofertaRepository.save(oferta);
        
        // Notificar hitos
        if (oferta.getSparkScore() % 50 == 0 && oferta.getSparkScore() > 0) {
            notificacionService.notificarHitoSpark(oferta);
        }
    }
    
    // Métodos básicos
    public List<Oferta> findAll() {
        return ofertaRepository.findAll();
    }
    
    public Optional<Oferta> findById(Integer id) {
        return ofertaRepository.findById(id);
    }
    
    public Oferta save(Oferta oferta) {
        return ofertaRepository.save(oferta);
    }
    
    public void deleteById(Integer id) {
        ofertaRepository.deleteById(id);
    }
    
    // Ofertas especiales
    public List<Oferta> obtenerDestacadas() {
        LocalDateTime hace7dias = LocalDateTime.now().minusDays(7);
        return ofertaRepository.findDestacadas(hace7dias, org.springframework.data.domain.PageRequest.of(0, 10));
    }
    
    public List<Oferta> obtenerTrending() {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        return ofertaRepository.findTrending(hace24h, org.springframework.data.domain.PageRequest.of(0, 15));
    }
    
    public List<Oferta> obtenerTopSpark() {
        return ofertaRepository.findTopBySparkScore(org.springframework.data.domain.PageRequest.of(0, 20));
    }
    
    public List<Oferta> obtenerProximasExpirar() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24h = ahora.plusHours(24);
        return ofertaRepository.findProximasExpirar(ahora, en24h);
    }
    
    @Transactional
    public void incrementarVistas(Integer id) {
        ofertaRepository.findById(id).ifPresent(oferta -> {
            oferta.incrementarVistas();
            ofertaRepository.save(oferta);
        });
    }
    
    @Transactional
    public void incrementarCompartidos(Integer id) {
        ofertaRepository.findById(id).ifPresent(oferta -> {
            oferta.incrementarCompartidos();
            ofertaRepository.save(oferta);
        });
    }
}