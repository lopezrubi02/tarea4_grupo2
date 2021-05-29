package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.MetodosDePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetodosDePagoRepository extends JpaRepository<MetodosDePago,Integer> {
}
