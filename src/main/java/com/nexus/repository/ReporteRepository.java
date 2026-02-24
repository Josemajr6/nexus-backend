package com.nexus.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexus.entity.EstadoReporte;
import com.nexus.entity.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    List<Reporte> findByEstadoOrderByFechaReporteAsc(EstadoReporte estado);
    List<Reporte> findByReportadorIdOrderByFechaReporteDesc(Integer reportadorId);
    boolean existsByReportadorIdAndProductoDenunciadoId(Integer reportadorId, Integer productoId);
    boolean existsByReportadorIdAndOfertaDenunciadaId(Integer reportadorId, Integer ofertaId);
}