package com.example.tarea4_grupo2.repository;


import com.example.tarea4_grupo2.dto.MontoTotal_PedidoHasPlatoDTO;
import com.example.tarea4_grupo2.entity.PedidoHasPlato;
import com.example.tarea4_grupo2.entity.PedidoHasPlatoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoHasPlatoRepository extends JpaRepository<PedidoHasPlato, PedidoHasPlatoKey> {

    PedidoHasPlato findAllByIdEquals(int idkey);

    List<PedidoHasPlato> findAllByPedidoIdpedidos(int idpedido);

    @Query(value = "select sum((pepla.cantidadplatos * p.precio)) as preciototal\n" +
            "from proyecto.pedidos_has_plato pepla \n" +
            "inner join proyecto.plato p on (pepla.plato_idplato = p.idplato)\n" +
            "where pepla.pedidos_idpedidos = ?1", nativeQuery = true)
    MontoTotal_PedidoHasPlatoDTO montototal(int idpedido);

}
