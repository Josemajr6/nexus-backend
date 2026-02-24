package com.nexus.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.Oferta;
import com.nexus.repository.OfertaRepository;

/**
 * Recalcula y publica el ranking de SparkVotos cada 5 minutos.
 *
 * FIX v4: Usa sparkCount - dripCount (no sparkScore).
 * Solo procesa ofertas activas para eficiencia.
 *
 * Angular suscripción:
 *   client.subscribe('/topic/ranking', msg => {
 *     this.topOfertas = JSON.parse(msg.body);
 *   });
 */
@Component
@EnableScheduling
public class UpvoteRankingScheduler {

    @Autowired private OfertaRepository      ofertaRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 300_000)
    @Transactional(readOnly = true)
    public void publicarRanking() {
        try {
            List<Map<String, Object>> top10 = ofertaRepository
                .findTop10ByOrderBySparkScoreDesc(PageRequest.of(0, 10))
                .stream()
                .map(o -> Map.of(
                    "id",         (Object) o.getId(),
                    "titulo",     o.getTitulo(),
                    "sparkCount", o.getSparkCount(),
                    "dripCount",  o.getDripCount(),
                    "score",      o.getSparkCount() - o.getDripCount(),
                    "badge",      o.getBadge() != null ? o.getBadge().name() : "",
                    "imagen",     o.getImagenPrincipal() != null ? o.getImagenPrincipal() : ""
                ))
                .collect(Collectors.toList());

            messagingTemplate.convertAndSend("/topic/ranking", top10);

        } catch (Exception e) {
            System.err.println("⚠️ UpvoteRankingScheduler error: " + e.getMessage());
        }
    }

    /** Limpia ofertas expiradas (se ejecuta cada hora) */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void limpiarExpiradas() {
        try {
            List<Oferta> expiradas = ofertaRepository.findExpiradas(LocalDateTime.now());
            expiradas.forEach(o -> o.setEsActiva(false));
            ofertaRepository.saveAll(expiradas);
            if (!expiradas.isEmpty())
                System.out.println("🧹 Desactivadas " + expiradas.size() + " ofertas expiradas");
        } catch (Exception e) {
            System.err.println("⚠️ Error limpiando expiradas: " + e.getMessage());
        }
    }
}