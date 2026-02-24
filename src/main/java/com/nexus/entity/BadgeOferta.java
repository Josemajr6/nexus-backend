package com.nexus.entity;

/**
 * Badge visual de una oferta.
 * Se muestra como etiqueta de color en Angular.
 *
 * CHOLLAZO    → Oferta excepcional (rojo)
 * DESTACADA   → Curada por el equipo (naranja)
 * NUEVA       → Publicada hace menos de 1 hora (verde)
 * EXPIRA_HOY  → Expira en menos de 24h (amarillo)
 * GRATUITA    → Precio = 0€ (azul)
 * PORCENTAJE  → Descuento muy alto (morado)
 */
public enum BadgeOferta {
    CHOLLAZO,
    DESTACADA,
    NUEVA,
    EXPIRA_HOY,
    GRATUITA,
    PORCENTAJE
}