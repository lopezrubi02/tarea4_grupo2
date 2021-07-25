package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Distritos;
import com.example.tarea4_grupo2.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistritosRepository extends JpaRepository<Distritos, Integer> {

    Distritos findByNombredistrito(String nombredistrito);

}
