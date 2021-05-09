package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidosRepository extends JpaRepository<Pedidos, Integer> {

}
