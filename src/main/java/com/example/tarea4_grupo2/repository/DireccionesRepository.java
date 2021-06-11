package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DireccionesRepository extends JpaRepository<Direcciones, Integer> {

    //@Query(value = "select * from direcciones where usuariosIdusuarios = ?1",nativeQuery = true)
    //List<Direcciones> findAllByUsuariosIdusuariosEquals(int idusuario);

    List<Direcciones> findAllByUsuario_Idusuarios(int idusuarios);  //TODO revisar si hay error

    @Query(value = "select * from direcciones where iddirecciones = ?1", nativeQuery = true)
    Direcciones findDireccionById(int id);


    //@Query(value = "select * from Direcciones where iddirecciones = ?1 and usuariosIdusuarios = ?2", nativeQuery = true)
    //Direcciones findDireccionesByIddireccionesAndUsuariosIdusuariosEquals(int iddireccion, int idcliente);

    Direcciones findDireccionesByIddireccionesAndUsuario_Idusuarios(int iddireccion, int idcliente);

    //Direcciones findByUsuariosIdusuarios(int usuarios_idusuarios);

    Direcciones findByUsuario(Usuario usuario);

//    List<Direcciones> findAllByUsuariosIdusuariosAndActivoEquals(int idusuario,int activo);

    List<Direcciones> findAllByUsuarioAndActivoEquals(Usuario usuario, int activo);
}
