package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByCuentaactivaEquals(Integer cuenta_activa);
    List<Usuario> findAllByRolAndCuentaactiva(String rol, Integer cuenta_activa);
    List<Usuario> findAllByRolAndCuentaactivaAndNombre(String rol, Integer cuenta_activa, String name);
    List<Usuario> findAllByNombreAndCuentaactiva(String nombre, Integer cuenta_activa);

    //Gestion de Nuevas Cuentas
    List<Usuario> findAllByRolAndNombreAndCuentaactiva(String rol, String nombre, Integer cuenta_activa);

    //@Query(value = "update usuario u set contrasenia_hash =sha2(?1,256) where idusuarios = ?2;",nativeQuery = true )
    //Usuario updateContraUsuario(String contrasenia,int idusuario);

    @Query(value= "insert into usuarios (idusuario, nombre, apellidos, email, contrasenia_hash,telefono,fecha_nacimiento,sexo,dni,rol) " +
            "values(?1,?2,?3,?4,sha2(?5,256),?6,?7,?8,?9,?10);",nativeQuery = true)
    Usuario nuevoUsuario(int id, String nombre, String apellido, String email, String contra, int telefono,
                         Date fecha, String sexo, String dni, String rol);

}
