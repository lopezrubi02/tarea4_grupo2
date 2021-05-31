package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DireccionesRepository extends JpaRepository<Direcciones, Integer> {

    List<Direcciones> findAllByUsuariosIdusuariosEquals(int idusuario);

    @Query(value = "select * from direcciones where iddirecciones = ?1", nativeQuery = true)
    Direcciones findDireccionById(int id);

    //TODO: usar esto en el controller del cliente, solo remplazar
    Direcciones findDireccionesByIddireccionesAndUsuariosIdusuariosEquals(int iddireccion, int idcliente);

    Direcciones findByUsuariosIdusuarios(int usuarios_idusuarios);

    List<Direcciones> findAllByUsuariosIdusuariosAndActivoEquals(int idusuario,int activo);
}
