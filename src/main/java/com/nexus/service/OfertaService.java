package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.dto.FiltroOfertaDTO;
import com.nexus.entity.Oferta;
import com.nexus.entity.SparkVoto;
import com.nexus.entity.Usuario;
import com.nexus.repository.OfertaRepository;
import com.nexus.repository.SparkVotoRepository;

@Service
public class OfertaService {
    
    @Autowired
    private OfertaRepository ofertaRepository;
    
    @Autowired
    private SparkVotoRepository sparkVotoRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
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
    
    // ⚡ SISTEMA SPARK - Votar oferta
    @Transactional
    public void votarOferta(Integer ofertaId, Integer usuarioId, Boolean esSpark) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(ofertaId);
        if (ofertaOpt.isEmpty()) {
            throw new IllegalArgumentException("Oferta no encontrada");
        }
        
        Oferta oferta = ofertaOpt.get();
        
        // Verificar si ya votó
        Optional<SparkVoto> votoExistente = sparkVotoRepository.findByUsuarioAndOferta(usuarioId, ofertaId);
        
        if (votoExistente.isPresent()) {
            SparkVoto voto = votoExistente.get();
            
            // Si es el mismo voto, quitarlo (toggle)
            if (voto.getEsSpark().equals(esSpark)) {
                if (esSpark) {
                    oferta.decrementarSpark();
                } else {
                    oferta.decrementarDrip();
                }
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
            // Crear nuevo voto
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            
            SparkVoto nuevoVoto = new SparkVoto(usuario, oferta, esSpark);
            sparkVotoRepository.save(nuevoVoto);
            
            if (esSpark) {
                oferta.incrementarSpark();
            } else {
                oferta.incrementarDrip();
            }
        }
        
        ofertaRepository.save(oferta);
        
        // Notificar hitos
        if (oferta.getSparkScore() == 50 || oferta.getSparkScore() == 100) {
            notificacionService.notificarHitoSpark(oferta);
        }
    }
    
    // Búsqueda con filtros
    public Page<Oferta> buscarConFiltros(FiltroOfertaDTO filtro, Pageable pageable) {
        return ofertaRepository.buscarConFiltros(
            filtro.getCategoria(),
            filtro.getTienda(),
            filtro.getPrecioMinimo(),
            filtro.getPrecioMaximo(),
            filtro.getBusqueda(),
            filtro.getSoloActivas() != null ? filtro.getSoloActivas() : true,
            pageable
        );
    }
    
    // Ofertas destacadas
    public List<Oferta> obtenerDestacadas() {
        LocalDateTime hace7dias = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, 10);
        return ofertaRepository.findDestacadas(hace7dias, pageable);
    }
    
    // Trending
    public List<Oferta> obtenerTrending() {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        Pageable pageable = PageRequest.of(0, 15);
        return ofertaRepository.findTrending(hace24h, pageable);
    }
    
    // Mejores Spark Score
    public List<Oferta> obtenerTopSpark() {
        Pageable pageable = PageRequest.of(0, 20);
        return ofertaRepository.findTopBySparkScore(pageable);
    }
    
    // Expiran pronto
    public List<Oferta> obtenerProximasExpirar() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24h = ahora.plusHours(24);
        return ofertaRepository.findProximasExpirar(ahora, en24h);
    }
    
    // Incrementar vistas
    @Transactional
    public void incrementarVistas(Integer id) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(id);
        if (ofertaOpt.isPresent()) {
            Oferta oferta = ofertaOpt.get();
            oferta.incrementarVistas();
            ofertaRepository.save(oferta);
        }
    }
    
    // Incrementar compartidos
    @Transactional
    public void incrementarCompartidos(Integer id) {
        Optional<Oferta> ofertaOpt = ofertaRepository.findById(id);
        if (ofertaOpt.isPresent()) {
            Oferta oferta = ofertaOpt.get();
            oferta.incrementarCompartidos();
            ofertaRepository.save(oferta);
        }
    }
    
    // Desactivar ofertas expiradas (Cron job)
    @Transactional
    public void desactivarExpiradas() {
        List<Oferta> expiradas = ofertaRepository.findExpiradas(LocalDateTime.now());
        for (Oferta oferta : expiradas) {
            oferta.setEsActiva(false);
        }
        ofertaRepository.saveAll(expiradas);
    }
}