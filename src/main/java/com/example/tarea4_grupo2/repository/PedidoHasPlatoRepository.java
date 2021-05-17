package com.example.tarea4_grupo2.repository;


import com.example.tarea4_grupo2.entity.PedidoHasPlato;
import com.example.tarea4_grupo2.entity.PedidoHasPlatoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoHasPlatoRepository extends JpaRepository<PedidoHasPlato, PedidoHasPlatoKey> {
}
