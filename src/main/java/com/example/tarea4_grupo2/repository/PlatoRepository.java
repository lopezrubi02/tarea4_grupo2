package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatoRepository extends JpaRepository<Plato,Integer> {

    @Query(value = "select * from plato p where p.restaurante_idrestaurante = ?1 and p.activo = 1",
            nativeQuery = true)
    List<Plato> buscarPlatosPorIdRestaurante(int idrestaurante);


}
