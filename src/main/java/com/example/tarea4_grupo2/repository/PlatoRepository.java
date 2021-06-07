package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatoRepository extends JpaRepository<Plato,Integer> {

    @Query(value = "select * from plato p where p.restaurante_idrestaurante = ?1 and p.activo = 1 order by p.disponibilidad desc",
            nativeQuery = true)
    List<Plato> buscarPlatosPorIdRestaurante(int idrestaurante);

    @Query(value = "select * from Plato where restaurante_idrestaurante = ?1 and disponibilidad = 1 and activo =1",
            nativeQuery = true)
    List<Plato> buscarPlatosPorIdRestauranteDisponilidadActivo(int idrestaurante);

    @Query(value = "select * from Plato p where lower(p.nombre) like concat('%',lower(:nombre),'%')",nativeQuery = true)
    List<Plato> buscarPlatoxNombre(@Param("nombre") String nombre);

    @Query(value = "select * from plato p where p.nombre = ?1 limit 1",nativeQuery = true)
    Optional<Plato> buscarPlato(String nombre);

}
