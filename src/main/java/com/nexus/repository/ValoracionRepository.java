package com.nexus.repository;
import com.nexus.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {
    List<Valoracion>     findByVendedorId(Integer vendedorId);
    List<Valoracion>     findByCompradorId(Integer compradorId);
    Optional<Valoracion> findByCompraId(Integer compraId);
    long                 countByVendedorId(Integer vendedorId);
    @Query("SELECT AVG(CAST(v.puntuacion AS double)) FROM Valoracion v WHERE v.vendedor.id = :vendedorId")
    Double findMediaByVendedorId(Integer vendedorId);
}
