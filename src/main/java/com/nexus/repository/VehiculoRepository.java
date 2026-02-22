package com.nexus.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.nexus.entity.EstadoProducto;
import com.nexus.entity.vehiculos.*;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    List<Vehiculo> findByTipoVehiculo(TipoVehiculo tipo);
    List<Vehiculo> findByEstadoProducto(EstadoProducto estado);

    @Query("SELECT v FROM Vehiculo v WHERE " +
           "(?1 IS NULL OR v.tipoVehiculo = ?1) AND " +
           "(?2 IS NULL OR LOWER(v.marca) LIKE LOWER(CONCAT('%',?2,'%'))) AND " +
           "(?3 IS NULL OR LOWER(v.modelo) LIKE LOWER(CONCAT('%',?3,'%'))) AND " +
           "(?4 IS NULL OR v.precio >= ?4) AND (?5 IS NULL OR v.precio <= ?5) AND " +
           "(?6 IS NULL OR v.anio >= ?6) AND (?7 IS NULL OR v.kilometros <= ?7) AND " +
           "(?8 IS NULL OR v.combustible = ?8) AND v.estadoProducto = 'DISPONIBLE' " +
           "ORDER BY v.precio ASC")
    List<Vehiculo> buscarConFiltros(TipoVehiculo tipo, String marca, String modelo,
        Double precioMin, Double precioMax, Integer anioMin, Integer kmMax, TipoCombustible combustible);

    @Query("SELECT DISTINCT v.marca FROM Vehiculo v WHERE v.estadoProducto = 'DISPONIBLE' ORDER BY v.marca")
    List<String> findMarcasDisponibles();

    @Query("SELECT v FROM Vehiculo v WHERE v.publicador.id = ?1 ORDER BY v.id DESC")
    List<Vehiculo> findByPublicadorId(Integer id);
}