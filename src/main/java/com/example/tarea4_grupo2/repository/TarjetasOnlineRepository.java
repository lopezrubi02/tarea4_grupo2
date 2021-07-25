package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.TarjetasOnline;
import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarjetasOnlineRepository extends JpaRepository<TarjetasOnline,Integer> {

    List<TarjetasOnline> findAllByClienteEquals(Usuario cliente);

    //para verificar si se ingresa una tarjeta repetida y no volver a guardar en la db
    List<TarjetasOnline> findAllByNumerotarjetaAndClienteEquals(String numero, Usuario cliente);

    List<TarjetasOnline> findAllByIdtarjetasonlineAndClienteEquals(int idtarjeta, Usuario cliente);

}
