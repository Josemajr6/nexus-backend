package com.nexus.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexus.entity.Mensaje;
import com.nexus.entity.Producto;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    // Recuperar todos los mensajes asociados a un producto
    List<Mensaje> findByProducto(Producto producto);
    
    // MEJORA CHAT: Mensajes ordenados por fecha ascendente (del más viejo al más nuevo)
    // Esto es vital para que parezca una conversación real
    List<Mensaje> findByProductoIdOrderByFechaCreacionAsc(int productoId);
}