package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepartidorRepository  extends JpaRepository<Repartidor, Integer> {

    @Query(value = "select * from datosrepartidor where usuariosidusuarios = ?1",nativeQuery = true)
    Repartidor findRepartidorByIdusuariosEquals(int idusuario);


    //
    @Query(value="select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, d2.nombredistrito as restaurantedistrito, d.direccion as clienteubicacion\n" +
            "    from pedidos p\n" +
            "    inner join restaurante r on (p.restauranteidrestaurante=r.idrestaurante)\n" +
            "    inner join direcciones d on (p.direccionentrega = d.iddirecciones)\n" +
            "    inner join distritos d2 on (d.iddistrito = d2.iddistritos)\n" +
            "    inner join restaurante r2 on (r2.iddistrito= d2.iddistritos)\n" +
            "    where p.idrepartidor=?1 group by p.idpedidos;\n", nativeQuery = true)
    List<PedidosReporteDTOs> findPedidosPorRepartidor(int idRepartidor);


    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre,\n" +
            "       d2.nombredistrito as restaurantedistrito, d.direccion as clienteubicacion\n" +
            "from pedidos p\n" +
            "         inner join restaurante r on (p.restauranteidrestaurante=r.idrestaurante)\n" +
            "         inner join direcciones d on (d.iddirecciones = p.direccionentrega )\n" +
            "         inner join distritos d2 on (d2.iddistritos = d.iddistrito)\n" +
            "where (d2.nombredistrito like %?1% or r.nombre like %?1%) and p.idrepartidor = ?2\n", nativeQuery = true)
    List <PedidosReporteDTOs> findReporte(String valorBuscado, int idRepartidor);

    /*@Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.nombre like CONCAT(?1,'%')",
            nativeQuery = true)
    List<PedidosReporteDTOs> findPedidosByRestaurante(String nombreRestaurante);*/

    /*@Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.distrito like CONCAT(?1,'%')",
            nativeQuery = true)
    List<PedidosReporteDTOs> findPedidosByDistrito(String distritoRestaurante);*/

    //Listo
    @Query(value = "SELECT sum(comisionrepartidor) as 'comision_mensual',month(fechahorapedido) as 'mes'," +
            "year(fechahorapedido) as 'year'\n" +
            "FROM proyecto.pedidos \n" +
            "where (idrepartidor=?1) ",nativeQuery = true)
    List<RepartidorComisionMensualDTO> obtenerComisionPorMes(int id);

    //Listo
    @Query(value = "select p.idpedidos ,r.nombre as restaurante, d2.nombredistrito as distritorestaurante,d.direccion as direccioncliente, p.comisionrepartidor as comision, p.montototal as monto\n" +
            "from pedidos p\n" +
            "    inner join  restaurante r on (p.restauranteIdrestaurante = r.idrestaurante)\n" +
            "    inner join direcciones d on (p.direccionentrega = d.iddirecciones)\n" +
            "    inner join distritos d2 on (d.iddistrito = d2.iddistritos)\n" +
            "where p.estadorepartidor like concat('pendient', '%')", nativeQuery = true)
    List<PedidosDisponiblesDTO> findListaPedidosDisponibles();


    //Usado por Adminsistema en reportes de repartidores

    @Query(value = "select u.nombre, u.apellidos,u.dni, dr.movilidad, dr.idrepartidor, count(p.idpedidos) as 'pedidos', " +
            "sum(p.comisionrepartidor) as 'comision' from usuarios u\n" +
            "inner join datosrepartidor dr on (u.idusuarios = dr.usuariosidusuarios)\n" +
            "inner join pedidos p on (p.idrepartidor = dr.usuariosidusuarios)\n" +
            "group by dr.idrepartidor\n" +
            "order by idrepartidor",nativeQuery = true)
    List<RepartidoresReportes_DTO> reporteRepartidores();

    @Query(value = "select u.nombre, u.apellidos,u.dni, dr.movilidad, dr.idrepartidor, count(p.idpedidos) as 'pedidos', " +
            "sum(p.comisionrepartidor) as 'comision' from usuarios u\n" +
            "inner join datosrepartidor dr on (u.idusuarios = dr.usuariosidusuarios)\n" +
            "inner join pedidos p on (p.idrepartidor = dr.usuariosidusuarios)\n" +
            "where (u. nombre like ?1 or u.apellidos like ?1) group by dr.idrepartidor\n" +
            "order by idrepartidor",nativeQuery = true)
    List<RepartidoresReportes_DTO> reporteRepartidores2(String buscar);

    //Listo
    @Query(value = "select pe.idpedidos, pe.montototal, pe.comisionrepartidor, pe.restauranteIdrestaurante, php.cantidadplatos, pl.idplato, pl.nombre\n" +
            "from pedidoshasplato php\n" +
            "    inner join pedidos pe on (pe.idpedidos=php.pedidosidpedidos) inner join plato pl on " +
            "(pl.idplato=php.platoidplato)\n" +
            "    where pe.idpedidos= ?1", nativeQuery = true)
    List<PlatosPorPedidoDTO> findListaPlatosPorPedido(int id);

}
