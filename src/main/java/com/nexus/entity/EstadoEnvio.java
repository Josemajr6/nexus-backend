package com.nexus.entity;

public enum EstadoEnvio {
    PENDIENTE_ENVIO,      // Pago confirmado, el vendedor aún no ha enviado
    ENVIADO,              // Vendedor marcó como enviado (tiene número de seguimiento)
    EN_TRANSITO,          // En camino (actualización del transportista)
    ENTREGADO,            // Comprador confirmó la recepción → fondos liberados
    INCIDENCIA,           // Problema con el envío (perdido, dañado)
    DEVOLUCION_SOLICITADA, // Comprador solicitó devolución
    DEVUELTO              // Producto devuelto, reembolso procesado
}