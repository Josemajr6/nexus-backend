package com.nexus.entity;

/**
 * Estados del ciclo de vida completo de una compra con pago seguro tipo escrow.
 *
 * Flujo ENVÍO:
 *   PENDIENTE → PAGADO → ENVIADO → ENTREGADO → COMPLETADA
 *
 * Flujo EN PERSONA:
 *   PENDIENTE → PAGADO → ENTREGA_ACORDADA → COMPLETADA
 *
 * Flujo cancelación/disputa:
 *   Cualquier estado → CANCELADA | EN_DISPUTA → REEMBOLSADA
 */
public enum EstadoCompra {
    PENDIENTE,          // PaymentIntent creado, el comprador no ha pagado aún
    PAGADO,             // Pago confirmado en Stripe, dinero en escrow
    ENVIADO,            // Vendedor ha marcado el pedido como enviado
    ENTREGA_ACORDADA,   // Para ventas en persona: quedada confirmada por ambas partes
    ENTREGADO,          // Comprador ha confirmado la recepción
    COMPLETADA,         // Fondos liberados al vendedor, transacción finalizada
    CANCELADA,          // Cancelada antes del envío, reembolso automático
    EN_DISPUTA,         // Comprador abrió disputa (producto no llegó, no es como se describe)
    REEMBOLSADA         // Reembolso procesado en Stripe
}