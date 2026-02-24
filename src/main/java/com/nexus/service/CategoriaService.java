package com.nexus.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexus.entity.Categoria;
import com.nexus.repository.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> getRaizActivas()         { return categoriaRepository.findRaizActivas(); }
    public List<Categoria> getTodas()               { return categoriaRepository.findByActivaTrueOrderByNombreAsc(); }
    public List<Categoria> getHijas(Integer pid)    { return categoriaRepository.findByParentIdAndActivaTrue(pid); }
    public Optional<Categoria> findBySlug(String s) { return categoriaRepository.findBySlug(s); }
    public Optional<Categoria> findById(Integer id) { return categoriaRepository.findById(id); }

    @Transactional
    public Categoria crear(Categoria categoria) {
        if (categoria.getSlug() != null
                && categoriaRepository.findBySlug(categoria.getSlug()).isPresent())
            throw new IllegalArgumentException("Ya existe una categoria con ese slug");
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Integer id, Categoria datos) {
        Categoria c = categoriaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        if (datos.getNombre()      != null) c.setNombre(datos.getNombre());
        if (datos.getDescripcion() != null) c.setDescripcion(datos.getDescripcion());
        if (datos.getIcono()       != null) c.setIcono(datos.getIcono());
        if (datos.getColor()       != null) c.setColor(datos.getColor());
        if (datos.getOrden()       != null) c.setOrden(datos.getOrden());
        if (datos.getActiva()      != null) c.setActiva(datos.getActiva());
        return categoriaRepository.save(c);
    }

    @Transactional
    public void eliminar(Integer id) {
        categoriaRepository.deleteById(id);
    }
}