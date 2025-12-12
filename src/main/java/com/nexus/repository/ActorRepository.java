package com.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.entity.Actor;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer>{

	Optional<Actor> findByUser(String user);
    Optional<Actor> findByEmail(String email);
	
}
