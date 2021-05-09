package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.PedidosDisponiblesDTO;
import com.example.tarea4_grupo2.dto.RepartidorComisionMensualDTO;
import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepartidorRepository  extends JpaRepository<Repartidor, Integer> {

    Repartidor findRepartidorByIdusuariosEquals(int idusuario);

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

    @Query(value = "SELECT sum(comisionrepartidor) as 'comision_mensual',month(fechahorapedido) as 'mes',year(fechahorapedido) as 'year'\n" +
            "FROM proyecto.pedidos \n" +
            "where (idrepartidor=?1  ) ",nativeQuery = true)
    List<RepartidorComisionMensualDTO> obtenerComisionPorMes(int id);

    @Query(value = "select p.idpedidos ,r.nombre as restaurante, r.distrito as distritorestaurante, d.direccion as direccioncliente, p.comisionrepartidor as comision, p.montototal as monto\n" +
            "            from\n" +
            "            pedidos p\n" +
            "               inner join  restaurante r on (p.restaurante_idrestaurante = r.idrestaurante)\n" +
            "               inner join direcciones d on (p.direccionentrega = d.iddirecciones)\n" +
            "where p.estadorepartidor = '0'", nativeQuery = true)
    List<PedidosDisponiblesDTO> findListaPedidosDisponibles();

}
