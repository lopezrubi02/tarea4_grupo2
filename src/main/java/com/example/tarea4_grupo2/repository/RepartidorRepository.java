package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepartidorRepository  extends JpaRepository<Repartidor, Integer> {
    Repartidor findRepartidorByUsuariosIdusuariosEquals(int idusuario);

}
