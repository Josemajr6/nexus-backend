package com.nexus.entity;
public enum MotivoDevolucion {
    PRODUCTO_NO_CORRESPONDE,  // No es lo que se anunciaba
    DEFECTUOSO,               // Tiene defectos no mencionados
    DAÑADO_EN_ENVIO,          // Se rompió durante el transporte
    ARREPENTIMIENTO,          // El comprador se arrepintió (vendedor puede rechazar)
    FRAUDE                    // Producto falso o diferente
}