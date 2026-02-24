package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.*;
import com.nexus.repository.*;

@Service
public class ValoracionService {

    @Autowired private ValoracionRepository  valoracionRepository;
    @Autowired private CompraRepository      compraRepository;
    @Autowired private ActorRepository       actorRepository;
    @Autowired private UsuarioRepository     usuarioRepository;
    @Autowired private NotificacionService   notificacionService; // FIX: usa NotificacionService real

    @Transactional
    public Valoracion valorar(Integer compraId, Integer compradorId,
                               Integer estrellas, String comentario) {
        if (estrellas < 1 || estrellas > 5)
            throw new IllegalArgumentException("Las estrellas deben ser entre 1 y 5");

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

        if (compra.getEstado() != EstadoCompra.COMPLETADA && compra.getEstado() != EstadoCompra.ENTREGADO)
            throw new IllegalStateException("Solo puedes valorar compras completadas");

        if (!compra.getComprador().getId().equals(compradorId))
            throw new IllegalStateException("No eres el comprador de esta compra");

        if (valoracionRepository.findByCompraId(compraId).isPresent())
            throw new IllegalStateException("Ya has valorado esta compra");

        Valoracion v = new Valoracion();
        v.setCompra(compra);
        v.setComprador(compra.getComprador());
        v.setVendedor(compra.getProducto().getPublicador());
        v.setEstrellas(estrellas);
        v.setComentario(comentario);
        Valoracion guardada = valoracionRepository.save(v);

        // Actualizar reputación del vendedor
        actualizarReputacion(compra.getProducto().getPublicador().getId());

        // Notificación al vendedor (sin Firebase, usa WebSocket + email)
        notificacionService.notificarNuevaValoracion(
            compra.getProducto().getPublicador().getId(),
            estrellas,
            compra.getProducto().getTitulo()
        );

        return guardada;
    }

    @Transactional
    public Valoracion responder(Integer valoracionId, Integer vendedorId, String respuesta) {
        Valoracion v = valoracionRepository.findById(valoracionId)
                .orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));
        if (!v.getVendedor().getId().equals(vendedorId))
            throw new IllegalStateException("No eres el vendedor de esta valoración");
        v.setRespuestaVendedor(respuesta);
        v.setFechaRespuesta(LocalDateTime.now());
        return valoracionRepository.save(v);
    }

    public List<Valoracion> getValoracionesVendedor(Integer vendedorId) {
        return valoracionRepository.findByVendedorIdOrderByFechaValoracionDesc(vendedorId);
    }

    public List<Valoracion> getMisValoraciones(Integer compradorId) {
        return valoracionRepository.findByCompradorIdOrderByFechaValoracionDesc(compradorId);
    }

    public Map<String, Object> getResumenVendedor(Integer vendedorId) {
        Double media = valoracionRepository.calcularMediaEstrellasVendedor(vendedorId);
        Long   total = valoracionRepository.contarResenasVendedor(vendedorId);
        return Map.of(
            "vendedorId",     vendedorId,
            "mediaEstrellas", media != null ? Math.round(media * 10.0) / 10.0 : 0.0,
            "totalResenas",   total
        );
    }

    private void actualizarReputacion(Integer vendedorId) {
        Double media = valoracionRepository.calcularMediaEstrellasVendedor(vendedorId);
        if (media == null) return;
        actorRepository.findById(vendedorId).ifPresent(actor -> {
            if (actor instanceof Usuario u) {
                u.setReputacion(Math.round(media * 10.0) / 10.0);
                usuarioRepository.save(u);
            }
        });
    }
}