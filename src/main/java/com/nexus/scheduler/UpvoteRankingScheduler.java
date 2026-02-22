package com.nexus.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.Oferta;
import com.nexus.repository.OfertaRepository;
import com.nexus.repository.VotoRepository;

/**
 * Recalcula el ranking de ofertas por votos cada 5 minutos.
 * Publica el top 10 por WebSocket → Angular actualiza la sección "Lo más votado".
 *
 * Angular se suscribe:
 *   client.subscribe('/topic/ranking', (msg) => {
 *     this.topOfertas = JSON.parse(msg.body);
 *   });
 */
@Component
@EnableScheduling
public class UpvoteRankingScheduler {

    @Autowired private OfertaRepository      ofertaRepository;
    @Autowired private VotoRepository        votoRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    /**
     * Cada 5 minutos (300_000 ms).
     * Configurable en application.properties: nexus.upvotes.recalculo-minutos
     * Para tests usar fixedDelay = 60_000 (1 minuto)
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void recalcularRanking() {
        try {
            List<Oferta> ofertas = ofertaRepository.findAll();

            ofertas.forEach(o -> {
                int score = votoRepository.sumarVotosPorOferta(o.getId());
                o.setSparkScore(score);
            });

            ofertaRepository.saveAll(ofertas);

            // Top 10 para publicar por WebSocket
            List<Object> top10 = ofertaRepository
                .findTop10ByOrderBySparkScoreDesc()
                .stream()
                .map(o -> java.util.Map.of(
                    "id",         o.getId(),
                    "titulo",     o.getTitulo(),
                    "sparkScore", o.getSparkScore(),
                    "imagen",     o.getImagenPrincipal() != null ? o.getImagenPrincipal() : ""
                ))
                .collect(java.util.stream.Collectors.toList());

            messagingTemplate.convertAndSend("/topic/ranking", top10);

        } catch (Exception e) {
            System.err.println("⚠️ Error en UpvoteRankingScheduler: " + e.getMessage());
        }
    }
}