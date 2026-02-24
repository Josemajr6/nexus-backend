package com.nexus.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.nexus.entity.Valoracion;

public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {

    List<Valoracion> findByVendedorIdOrderByFechaValoracionDesc(Integer vendedorId);
    List<Valoracion> findByCompradorIdOrderByFechaValoracionDesc(Integer compradorId);
    Optional<Valoracion> findByCompraId(Integer compraId);

    /** Media de estrellas del vendedor */
    @Query("SELECT AVG(v.estrellas) FROM Valoracion v WHERE v.vendedor.id = ?1")
    Double calcularMediaEstrellasVendedor(Integer vendedorId);

    /** Número total de reseñas del vendedor */
    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.vendedor.id = ?1")
    Long contarResenasVendedor(Integer vendedorId);
}