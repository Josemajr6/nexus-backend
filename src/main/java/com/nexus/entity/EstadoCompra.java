package com.nexus.entity;

public enum EstadoCompra {
    PENDIENTE,          // Compra iniciada, esperando pago
    PAGADO,             // Pago confirmado, fondos en escrow
    ENVIADO,            // Vendedor marcó como enviado
    EN_TRANSITO,        // En camino (actualización del transportista)
    ENTREGADO,          // Comprador confirmó recepción (o auto-confirmado a los 7 días)
    COMPLETADA,         // Fondos liberados al vendedor
    CANCELADA,          // Cancelada antes del pago
    EN_DISPUTA,         // Disputa abierta por el comprador
    REEMBOLSADA         // Reembolso procesado
}