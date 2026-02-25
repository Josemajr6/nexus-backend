package com.nexus.entity;
import jakarta.persistence.*;
/**
 * NUNCA acceder a actor.email/password/username directamente (son private en Actor).
 * Usar siempre los setters heredados: setUser(), setEmail(), setPassword()
 *
 * En PopulateDB:
 *   Admin a = new Admin();
 *   a.setUser("admin");
 *   a.setEmail("admin@nexus.test");
 *   a.setPassword(encode("..."));
 *   actorRepository.save(a);   <- actorRepository, NO usuarioRepository
 */
@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "actor_id")
public class Admin extends Actor {
    @Column(name = "nivel_acceso", nullable = false)
    private Integer nivelAcceso = 1;
    public Admin() {}
    public Integer getNivelAcceso()          { return nivelAcceso; }
    public void    setNivelAcceso(Integer n) { this.nivelAcceso = n; }
}
