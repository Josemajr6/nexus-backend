package com.nexus.entity;
public enum EstadoDevolucion {
    SOLICITADA,          // Comprador pide la devolución
    ACEPTADA,            // Vendedor acepta
    RECHAZADA,           // Vendedor rechaza (puede apelar al admin)
    DEVOLUCION_ENVIADA,  // Comprador envió el producto de vuelta
    COMPLETADA,          // Vendedor confirmó recepción → reembolso procesado
    EN_DISPUTA           // Admin interviene
}