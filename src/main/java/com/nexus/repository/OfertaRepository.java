package com.nexus.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nexus.entity.BadgeOferta;
import com.nexus.entity.Oferta;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {
    
    // Básicas
    List<Oferta> findByEsActivaTrue();
    List<Oferta> findByCategoria(String categoria);
    List<Oferta> findByTiendaContainingIgnoreCase(String tienda);
    List<Oferta> findByBadgeAndEsActivaTrue(BadgeOferta badge);
    
    // Búsqueda por texto
    @Query("SELECT o FROM Oferta o WHERE LOWER(o.titulo) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(o.descripcion) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Oferta> buscarPorTexto(String texto);
    
    // Filtros avanzados (con paginación)
    @Query("SELECT o FROM Oferta o WHERE " +
           "(?1 IS NULL OR o.categoria = ?1) AND " +
           "(?2 IS NULL OR LOWER(o.tienda) LIKE LOWER(CONCAT('%', ?2, '%'))) AND " +
           "(?3 IS NULL OR o.precioOferta >= ?3) AND " +
           "(?4 IS NULL OR o.precioOferta <= ?4) AND " +
           "(?5 IS NULL OR LOWER(o.titulo) LIKE LOWER(CONCAT('%', ?5, '%'))) AND " +
           "(?6 = false OR o.esActiva = true)")
    Page<Oferta> buscarConFiltros(String categoria, String tienda, Double precioMin, Double precioMax, String busqueda, boolean soloActivas, Pageable pageable);
    
    // Top Spark Score
    @Query("SELECT o FROM Oferta o WHERE o.esActiva = true ORDER BY (o.sparkCount - o.dripCount) DESC")
    List<Oferta> findTopBySparkScore(Pageable pageable);
    
    // Trending (últimas 24h)
    @Query("SELECT o FROM Oferta o WHERE o.esActiva = true AND o.fechaPublicacion >= ?1 ORDER BY (o.sparkCount - o.dripCount) DESC")
    List<Oferta> findTrending(LocalDateTime hace24h, Pageable pageable);
    
    // Recientes
    @Query("SELECT o FROM Oferta o WHERE o.esActiva = true ORDER BY o.fechaPublicacion DESC")
    List<Oferta> findRecientes(Pageable pageable);
    
    // Expiran pronto
    @Query("SELECT o FROM Oferta o WHERE o.fechaExpiracion BETWEEN ?1 AND ?2 AND o.esActiva = true ORDER BY o.fechaExpiracion ASC")
    List<Oferta> findProximasExpirar(LocalDateTime ahora, LocalDateTime en24h);
    
    // Expiradas
    @Query("SELECT o FROM Oferta o WHERE o.fechaExpiracion < ?1 AND o.esActiva = true")
    List<Oferta> findExpiradas(LocalDateTime ahora);
    
    // Por actor
    @Query("SELECT o FROM Oferta o WHERE o.actor.id = ?1 ORDER BY o.fechaPublicacion DESC")
    List<Oferta> findByActorId(Integer actorId);
    
    // Destacadas (Spark alto + Descuento + Reciente)
    @Query("SELECT o FROM Oferta o WHERE o.esActiva = true AND (o.sparkCount - o.dripCount) >= 10 AND o.fechaPublicacion >= ?1 AND o.precioOriginal > 0 ORDER BY (o.sparkCount - o.dripCount) DESC")
    List<Oferta> findDestacadas(LocalDateTime hace7dias, Pageable pageable);
    
    // Estadísticas
    @Query("SELECT COUNT(o) FROM Oferta o WHERE o.esActiva = true")
    long countActivas();
}