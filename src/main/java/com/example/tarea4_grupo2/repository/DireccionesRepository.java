package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionesRepository extends JpaRepository<Direcciones, Integer> {

    List<Direcciones> findAllByUsuariosIdusuariosEquals(int idusuario);

    Direcciones findByUsuariosIdusuarios(int usuarios_idusuarios);
}
