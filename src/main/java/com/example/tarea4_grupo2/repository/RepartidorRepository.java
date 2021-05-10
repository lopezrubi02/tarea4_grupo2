package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.PedidosDisponiblesDTO;
import com.example.tarea4_grupo2.dto.PlatosPorPedidoDTO;
import com.example.tarea4_grupo2.dto.RepartidorComisionMensualDTO;
import com.example.tarea4_grupo2.dto.RepartidoresReportes_DTO;
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


    //Usado por Adminsistema en reportes de repartidores

    @Query(value = "select u.nombre, u.apellidos,u.dni, dr.movilidad, dr.idrepartidor, count(p.idpedidos) as 'pedidos', sum(p.comisionrepartidor) as 'comision' from usuarios u\n" +
            "inner join datosrepartidor dr on (u.idusuarios = dr.usuarios_idusuarios)\n" +
            "inner join pedidos p on (p.idrepartidor = dr.usuarios_idusuarios)\n" +
            "group by dr.idrepartidor\n" +
            "order by idrepartidor",nativeQuery = true)
    List<RepartidoresReportes_DTO> reporteRepartidores();

    @Query(value = "select pe.idpedidos, pe.montototal, pe.comisionrepartidor, pe.restaurante_idrestaurante, php.cantidadplatos, pl.idplato, pl.nombre\n" +
            "from pedidos_has_plato php \n" +
            "inner join pedidos pe on (pe.idpedidos=php.pedidos_idpedidos)\n" +
            "inner join plato pl on (pl.idplato=php.plato_idplato)\n" +
            "where p.idpedidos= ?1", nativeQuery = true)
    List<PlatosPorPedidoDTO> findListaPlatosPorPedido(int id);

}
