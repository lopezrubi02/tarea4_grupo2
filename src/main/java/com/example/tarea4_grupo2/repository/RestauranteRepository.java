package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    Restaurante findRestauranteByIdadminrestEquals(int idAdmin);



}
