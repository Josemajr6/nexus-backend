package com.nexus.entity;

public enum EstadoSuscripcion {
    PENDIENTE,      // Suscrito pero pendiente de confirmar (double opt-in)
    ACTIVO,         // Confirmado, recibe emails
    BAJA,           // Se dio de baja (soft-delete, no se elimina el registro)
    BLOQUEADO       // Marcado como spam/bounce por el proveedor de email
}