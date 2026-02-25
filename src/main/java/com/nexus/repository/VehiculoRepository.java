package com.nexus.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.nexus.entity.EstadoVehiculo;
import com.nexus.entity.TipoCombustible;
import com.nexus.entity.TipoVehiculo;
import com.nexus.entity.Vehiculo;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    List<Vehiculo> findByPublicadorIdOrderByFechaPublicacionDesc(Integer publicadorId);

    List<Vehiculo> findByEstadoVehiculo(EstadoVehiculo estado);

    List<Vehiculo> findByTipoVehiculoAndEstadoVehiculo(TipoVehiculo tipo, EstadoVehiculo estado);

    @Query("SELECT v FROM Vehiculo v WHERE v.estadoVehiculo = 'DISPONIBLE' AND " +
           "(?1 IS NULL OR CAST(v.tipoVehiculo AS string) = ?1) AND " +
           "(?2 IS NULL OR LOWER(v.marca) LIKE LOWER(CONCAT('%', ?2, '%'))) AND " +
           "(?3 IS NULL OR v.anio >= ?3) AND (?4 IS NULL OR v.anio <= ?4) AND " +
           "(?5 IS NULL OR v.kilometros <= ?5) AND " +
           "(?6 IS NULL OR v.precio >= ?6) AND (?7 IS NULL OR v.precio <= ?7) AND " +
           "(?8 IS NULL OR LOWER(v.combustible) = LOWER(?8)) AND " +
           "(?9 IS NULL OR LOWER(v.cambio) = LOWER(?9))")
    Page<Vehiculo> buscarConFiltros(String tipo, String marca,
                                     Integer anioMin, Integer anioMax, Integer kmMax,
                                     Double precioMin, Double precioMax,
                                     String combustible, String cambio, Pageable pageable);

    @Query("SELECT DISTINCT v.marca FROM Vehiculo v " +
           "WHERE v.estadoVehiculo = 'DISPONIBLE' AND v.marca IS NOT NULL ORDER BY v.marca")
    List<String> findMarcasDistintas();
    
    @Query("SELECT v FROM Vehiculo v WHERE ... ") // Tu query aquí
    Page<Vehiculo> buscarPaginado(TipoVehiculo tipo, String marca, String modelo,
                                  Double precioMin, Double precioMax, Integer anioMin,
                                  Integer kmMax, TipoCombustible combustible, Pageable pageable);
}