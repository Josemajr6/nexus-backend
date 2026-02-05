package com.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nexus.entity.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = ?1 ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findByUsuarioId(Integer usuarioId);
    
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = ?1 AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNoLeidasByUsuarioId(Integer usuarioId);
    
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.id = ?1 AND n.leida = false")
    long countNoLeidasByUsuarioId(Integer usuarioId);
}