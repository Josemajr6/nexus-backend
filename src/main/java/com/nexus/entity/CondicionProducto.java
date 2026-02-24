package com.nexus.entity;

public enum CondicionProducto {
    NUEVO,              // Sin usar, con etiquetas
    COMO_NUEVO,         // Usado menos de 5 veces, sin signos de uso
    MUY_BUEN_ESTADO,    // Alguna señal mínima de uso
    BUEN_ESTADO,        // Usado pero bien conservado
    ACEPTABLE           // Signos claros de uso
}