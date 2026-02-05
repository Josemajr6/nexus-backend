package com.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nexus.entity.SparkVoto;

@Repository
public interface SparkVotoRepository extends JpaRepository<SparkVoto, Integer> {
    
    @Query("SELECT sv FROM SparkVoto sv WHERE sv.usuario.id = ?1 AND sv.oferta.id = ?2")
    Optional<SparkVoto> findByUsuarioAndOferta(Integer usuarioId, Integer ofertaId);
}