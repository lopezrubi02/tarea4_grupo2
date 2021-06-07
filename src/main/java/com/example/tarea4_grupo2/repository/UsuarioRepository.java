package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.DatosDTO;
import com.example.tarea4_grupo2.entity.Direcciones;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByCuentaActivaEquals(Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaActiva(String rol, Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaActivaAndNombre(String rol, Integer cuentaActiva, String name);
    List<Usuario> findAllByNombreAndCuentaActiva(String nombre, int cuentaActiva);

    Usuario findByToken(String token);

    @Query(value = "select * from usuarios where idusuarios = ?1", nativeQuery = true)
    Usuario findUsuarioById(int id);

    //Gestion de Nuevas Cuentas

    List<Usuario> findAllByRolAndNombreAndCuentaActiva(String rol, String nombre, Integer cuentaActiva);

    @Query(value = "select * from usuarios u where cuentaActiva = 2 and (u.rol ='Repartidor' or u.rol = 'AdminRestaurante');",nativeQuery = true)
    List<Usuario> cuentasNuevas();

    @Query(value = "select * from usuarios u where cuentaActiva = 2 and (u.nombre like ?1 or u.apellidos like ?1 or u.dni like ?1) " +
            " and (u.rol ='Repartidor' or u.rol = 'AdminRestaurante') ;",nativeQuery = true)
    List<Usuario> buscarGestionCuentasNuevas(String buscar);

    @Query(value ="select * from usuarios \n" +
            "            where (rol = 'Cliente' or rol='AdminRestaurante' or rol='Repartidor') and cuentaactiva=1;",nativeQuery = true)
    List<Usuario> usuarioreportes();

    @Query(value= "insert into usuarios (idusuario, nombre, apellidos, email, contrasenia_hash,telefono,fecha_nacimiento,sexo,dni,rol) " +
            "values(?1,?2,?3,?4,sha2(?5,256),?6,?7,?8,?9,?10);",nativeQuery = true)
    Usuario nuevoUsuario(int id, String nombre, String apellido, String email, String contra, int telefono,
                         Date fecha, String sexo, String dni, String rol);

    Usuario findByDni(String dni);
    //para guardar direccion de usuario
    Usuario findByDniAndEmailEquals(String dni, String email);
    //Repartidor findByIdusuarios(int usuarios_idusuarios);
    Usuario findByEmail(String email);
    @Query(value="select nombre,apellidos,dni,fechanacimiento from usuarios where idusuarios=?1",nativeQuery = true)
    DatosDTO obtenerDatos(int id);

}
