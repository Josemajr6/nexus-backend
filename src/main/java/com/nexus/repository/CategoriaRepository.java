package com.nexus.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.nexus.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findBySlug(String slug);
    Optional<Categoria> findByNombre(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.parent IS NULL AND c.activa = true ORDER BY c.orden ASC")
    List<Categoria> findRaizActivas();

    List<Categoria> findByActivaTrueOrderByNombreAsc();

    List<Categoria> findByParentIdAndActivaTrue(Integer parentId);
}