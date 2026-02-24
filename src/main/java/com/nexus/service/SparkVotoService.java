package com.nexus.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.*;
import com.nexus.repository.*;

/**
 * FIX v4: Oferta usa sparkCount + dripCount (no sparkScore).
 * El score visible = sparkCount - dripCount.
 * Upvote (+1) incrementa sparkCount.
 * Downvote (-1) incrementa dripCount.
 */
@Service
public class SparkVotoService {

    @Autowired private SparkVotoRepository sparkVotoRepository;
    @Autowired private OfertaRepository    ofertaRepository;
    @Autowired private ProductoRepository  productoRepository;
    @Autowired private ActorRepository     actorRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public int votarOferta(Integer actorId, Integer ofertaId, int valor) {
        validarValor(valor);
        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada"));

        Optional<SparkVoto> prev = sparkVotoRepository.findByActorIdAndOfertaId(actorId, ofertaId);
        if (prev.isPresent()) {
            SparkVoto v = prev.get();
            if (v.getValor() == valor) {
                // Toggle: quitar el voto, deshacer el contador
                if (valor == 1) oferta.setSparkCount(Math.max(0, oferta.getSparkCount() - 1));
                else            oferta.setDripCount(Math.max(0, oferta.getDripCount() - 1));
                sparkVotoRepository.deleteByActorAndOferta(actorId, ofertaId);
            } else {
                // Cambiar voto: un contador sube, el otro baja
                if (valor == 1) {
                    oferta.setSparkCount(oferta.getSparkCount() + 1);
                    oferta.setDripCount(Math.max(0, oferta.getDripCount() - 1));
                } else {
                    oferta.setDripCount(oferta.getDripCount() + 1);
                    oferta.setSparkCount(Math.max(0, oferta.getSparkCount() - 1));
                }
                v.setValor(valor);
                sparkVotoRepository.save(v);
            }
        } else {
            Actor actor = actorRepository.findById(actorId)
                    .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado"));
            SparkVoto sv = new SparkVoto();
            sv.setActor(actor); sv.setOferta(oferta); sv.setValor(valor);
            sparkVotoRepository.save(sv);
            if (valor == 1) oferta.setSparkCount(oferta.getSparkCount() + 1);
            else            oferta.setDripCount(oferta.getDripCount() + 1);
        }

        ofertaRepository.save(oferta);
        int score = oferta.getSparkCount() - oferta.getDripCount();

        // Tiempo real: Angular actualiza el contador sin recargar
        messagingTemplate.convertAndSend("/topic/votos/oferta/" + ofertaId, Map.of(
            "ofertaId",    ofertaId,
            "sparkCount",  oferta.getSparkCount(),
            "dripCount",   oferta.getDripCount(),
            "score",       score
        ));
        return score;
    }

    @Transactional
    public int votarProducto(Integer actorId, Integer productoId, int valor) {
        validarValor(valor);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Optional<SparkVoto> prev = sparkVotoRepository.findByActorIdAndProductoId(actorId, productoId);
        if (prev.isPresent()) {
            SparkVoto v = prev.get();
            if (v.getValor() == valor) sparkVotoRepository.deleteByActorAndProducto(actorId, productoId);
            else { v.setValor(valor); sparkVotoRepository.save(v); }
        } else {
            Actor actor = actorRepository.findById(actorId)
                    .orElseThrow(() -> new IllegalArgumentException("Actor no encontrado"));
            SparkVoto sv = new SparkVoto();
            sv.setActor(actor); sv.setProducto(producto); sv.setValor(valor);
            sparkVotoRepository.save(sv);
        }

        int score = sparkVotoRepository.sumarVotosPorProducto(productoId);
        messagingTemplate.convertAndSend("/topic/votos/producto/" + productoId,
            Map.of("productoId", productoId, "score", score));
        return score;
    }

    public int getScoreOferta(Integer ofertaId) {
        return ofertaRepository.findById(ofertaId)
            .map(o -> o.getSparkCount() - o.getDripCount()).orElse(0);
    }

    public int getScoreProducto(Integer productoId) {
        return sparkVotoRepository.sumarVotosPorProducto(productoId);
    }

    public Integer getVotoActualOferta(Integer actorId, Integer ofertaId) {
        return sparkVotoRepository.findByActorIdAndOfertaId(actorId, ofertaId)
                .map(SparkVoto::getValor).orElse(0);
    }

    public Integer getVotoActualProducto(Integer actorId, Integer productoId) {
        return sparkVotoRepository.findByActorIdAndProductoId(actorId, productoId)
                .map(SparkVoto::getValor).orElse(0);
    }

    private void validarValor(int v) {
        if (v != 1 && v != -1) throw new IllegalArgumentException("Valor debe ser 1 (spark) o -1 (drip)");
    }
}