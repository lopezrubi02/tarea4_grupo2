package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepartidorRepository  extends JpaRepository<Repartidor, Integer> {
    Repartidor findRepartidorByUsuariosIdusuariosEquals(int idusuario);

    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.nombre like CONCAT(?1,'%')",
            nativeQuery = true)
    List<Pedidos> findPedidosByRestaurante(String nombre);

    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.distrito like CONCAT(?1,'%')",
            nativeQuery = true)
    List<Pedidos> findPedidosByDistrito(String distrito);

}
