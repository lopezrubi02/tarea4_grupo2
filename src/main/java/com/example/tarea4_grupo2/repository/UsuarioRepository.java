package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByCuentaActivaEquals(Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaActiva(String rol, Integer cuentaActiva);
    List<Usuario> findAllByRolAndCuentaActivaAndNombre(String rol, Integer cuentaActiva, String name);
    List<Usuario> findAllByNombreAndCuentaActiva(String nombre, int cuentaActiva);
}
