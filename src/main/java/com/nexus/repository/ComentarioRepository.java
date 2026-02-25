package com.nexus.repository;
import com.nexus.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByProductoIdOrderByFechaDesc(Integer productoId);
    List<Comentario> findByActorIdOrderByFechaDesc(Integer actorId);
    long countByProductoId(Integer productoId);
}
