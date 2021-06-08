package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Cupones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CuponesRepository extends JpaRepository<Cupones,Integer> {

    @Query(value = "select * from Cupones where idrestaurante = ?1",
            nativeQuery = true)
    List<Cupones> buscarCuponesPorIdRestaurante(int idrestaurante);
}
