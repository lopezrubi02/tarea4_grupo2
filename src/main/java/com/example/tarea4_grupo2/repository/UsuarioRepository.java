package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.DatosDTO;
import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByCuentaActivaEquals(Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaActiva(String rol, Integer cuentaActiva);
//    List<Usuario> findAllByRolAndCuentaActivaAndNombre(String rol, Integer cuentaActiva, String name);
  //  List<Usuario> findAllByNombreAndCuentaActiva(String nombre, int cuentaActiva);

    Usuario findByEmailEqualsAndRolEquals(String email, String rol);
    Usuario findByEmailEquals(String email);
    @Query(value = "SELECT * FROM usuarios u WHERE u.email = :username",nativeQuery = true)
    Usuario getUserByUsername(@Param("username") String username);

    Usuario findByToken(String token);

    @Query(value = "select * from usuarios where idusuarios = ?1", nativeQuery = true)
    Usuario findUsuarioById(int id);

    @Query(value = "select * from usuarios u where cuentaactiva = 1 and (u.nombre like ?1 or u.apellidos like ?1 or u.dni like ?1)",nativeQuery = true)
    List<Usuario> cuentasActuales(String nombre);

    @Query(value = "select * from usuarios u where cuentaactiva = 1 and (u.nombre like ?1 or u.apellidos like ?1 or u.dni like ?1) " +
            " and u.rol =?2",nativeQuery = true)
    List<Usuario> cuentasActualesRol(String nombre,String rol);

    //Gestion de Nuevas Cuentas

    List<Usuario> findAllByRolAndNombreAndCuentaActiva(String rol, String nombre, Integer cuentaActiva);

    @Query(value = "select * from usuarios u where (cuentaactiva = 2 and u.rol = 'AdminRestaurante') or (cuentaactiva = -1 and u.rol ='Repartidor')",nativeQuery = true)
    List<Usuario> cuentasNuevas();

    @Query(value = "select * from usuarios u where (cuentaactiva = 2 and u.rol = 'AdminRestaurante') or (cuentaactiva = -1 and u.rol ='Repartidor') " +
            "and (u.nombre like ?1 or u.apellidos like ?1 or u.dni like ?1) ",nativeQuery = true)
    List<Usuario> buscarGestionCuentasNuevas(String buscar);

    @Query(value ="select * from usuarios \n" +
            "            where cuentaactiva=1",nativeQuery = true)
    List<Usuario> usuarioreportes();

    Usuario findByDni(String dni);
    //para guardar direccion de usuario
    Usuario findByDniAndEmailEquals(String dni, String email);
    Usuario findByDniAndRolEquals(String dni, String rol);
    //Repartidor findByIdusuarios(int usuarios_idusuarios);
    Usuario findByEmail(String email);
    @Query(value="select nombre,apellidos,email,dni,fechanacimiento from usuarios where idusuarios=?1",nativeQuery = true)
    DatosDTO obtenerDatos(int id);
    Optional<Usuario> findByEmailAndAndRol(String email, String rol);
    Optional<Usuario> findByDniAndRol(String dni,String rol);

    @Query(value="select count(idusuarios) from usuarios where email=?1 and rol=?2",nativeQuery = true)
    Integer verificarEmail(String email,String rol);
}
