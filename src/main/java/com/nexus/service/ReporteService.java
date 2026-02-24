package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.*;
import com.nexus.repository.*;

@Service
public class ReporteService {

    @Autowired private ReporteRepository  reporteRepository;
    @Autowired private ActorRepository    actorRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private OfertaRepository   ofertaRepository;
    // FIX: ChatMensajeRepository añadido para reportes de mensajes
    @Autowired(required = false)
    private ChatMensajeRepository chatMensajeRepository;

    @Transactional
    public Reporte reportar(Integer reportadorId, TipoReporte tipo, MotivoReporte motivo,
                             String descripcion, Integer objetoId) {

        Actor reportador = actorRepository.findById(reportadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Reporte r = new Reporte();
        r.setReportador(reportador);
        r.setTipo(tipo);
        r.setMotivo(motivo);
        r.setDescripcion(descripcion);

        // FIX: switch en lugar de múltiples if, con null-safe orElseThrow
        switch (tipo) {
            case USUARIO -> r.setUsuarioDenunciado(
                actorRepository.findById(objetoId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")));
            case PRODUCTO -> r.setProductoDenunciado(
                productoRepository.findById(objetoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado")));
            case OFERTA -> r.setOfertaDenunciada(
                ofertaRepository.findById(objetoId)
                    .orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada")));
            case MENSAJE -> {
                if (chatMensajeRepository != null) {
                    r.setMensajeDenunciado(
                        chatMensajeRepository.findById(objetoId)
                            .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado")));
                }
            }
        }

        return reporteRepository.save(r);
    }

    public List<Reporte> getPendientes() {
        return reporteRepository.findByEstadoOrderByFechaReporteAsc(EstadoReporte.PENDIENTE);
    }

    @Transactional
    public Reporte resolver(Integer reporteId, EstadoReporte nuevoEstado, String notaAdmin) {
        Reporte r = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));
        r.setEstado(nuevoEstado);
        r.setNotaAdmin(notaAdmin);
        r.setFechaResolucion(LocalDateTime.now());
        return reporteRepository.save(r);
    }

    public List<Reporte> getMisReportes(Integer reportadorId) {
        return reporteRepository.findByReportadorIdOrderByFechaReporteDesc(reportadorId);
    }
}