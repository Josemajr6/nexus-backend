package com.nexus.repository;

import com.nexus.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Metodos requeridos por ReporteService:
 *   findByEstado(EstadoReporte)  line 33
 *   findByTipo(TipoReporte)      line 34
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByEstado(EstadoReporte estado);

    List<Reporte> findByTipo(TipoReporte tipo);

    List<Reporte> findByReportadorId(Integer reportadorId);

    List<Reporte> findByEstadoOrderByFechaDesc(EstadoReporte estado);
}