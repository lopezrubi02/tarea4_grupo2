package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Cupones;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuponesRepository extends JpaRepository<Cupones,Integer> {

    @Query(value = "select * from cupones where idrestaurante = ?1",
            nativeQuery = true)
    List<Cupones> buscarCuponesPorIdRestaurante(int idrestaurante);
}
