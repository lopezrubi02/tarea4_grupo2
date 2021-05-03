package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByCuentaactivaEquals(Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaactiva(String rol, Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaactivaAndNombre(String rol, Integer cuentaActiva, String name);
    List<Usuario> findAllByNombreAndCuentaactiva(String nombre, int cuentaActiva);


    //Gestion de Nuevas Cuentas

    List<Usuario> findAllByRolAndNombreAndCuentaactiva(String rol, String nombre, Integer cuentaActiva);

    @Query(value = "select * from usuarios u where cuentaactiva = 0 and (u.rol ='Repartidor' or u.rol = 'AdminRestaurante');",nativeQuery = true)
    List<Usuario> cuentasNuevas();

    @Query(value = "select * from usuarios u where cuentaactiva = 0 and (u.nombre like ?1 or u.apellidos like ?1) " +
            " and (u.rol ='Repartidor' or u.rol = 'AdminRestaurante') ;",nativeQuery = true)
    List<Usuario> buscarGestionCuentasNuevas(String buscar);

    @Query(value= "insert into usuarios (idusuario, nombre, apellidos, email, contrasenia_hash,telefono,fecha_nacimiento,sexo,dni,rol) " +
            "values(?1,?2,?3,?4,sha2(?5,256),?6,?7,?8,?9,?10);",nativeQuery = true)
    Usuario nuevoUsuario(int id, String nombre, String apellido, String email, String contra, int telefono,
                         Date fecha, String sexo, String dni, String rol);



}
