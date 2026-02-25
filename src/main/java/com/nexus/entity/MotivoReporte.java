package com.nexus.entity;
/**
 * Motivos para reportar contenido.
 * Requerido por ReporteService (setMotivo recibe MotivoReporte, no String)
 * y por ReporteController: reportar(Integer, TipoReporte, MotivoReporte, String, Integer)
 */
public enum MotivoReporte {
    SPAM,
    CONTENIDO_INAPROPIADO,
    PRODUCTO_FALSO,
    PRECIO_INCORRECTO,
    INFORMACION_FALSA,
    ACOSO,
    FRAUDE,
    DUPLICADO,
    PROHIBIDO,
    OTRO
}