package com.nexus.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.nexus.entity.NotificacionInApp;

public interface NotificacionRepository extends JpaRepository<NotificacionInApp, Integer> {

    Page<NotificacionInApp> findByReceptorIdOrderByFechaCreacionDesc(Integer receptorId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificacionInApp n WHERE n.receptor.id = ?1 AND n.leida = false")
    long countNoLeidasByReceptorId(Integer receptorId);

    @Modifying @Transactional
    @Query("UPDATE NotificacionInApp n SET n.leida = true WHERE n.receptor.id = ?1")
    void marcarTodasLeidasByReceptorId(Integer receptorId);

    @Modifying @Transactional
    @Query("UPDATE NotificacionInApp n SET n.leida = true WHERE n.id = ?1")
    void marcarLeidaById(Integer notificacionId);
}