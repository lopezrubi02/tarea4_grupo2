package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.PedidosDisponiblesDTO;
import com.example.tarea4_grupo2.dto.PedidosReporteDTO;
import com.example.tarea4_grupo2.dto.PlatosPorPedidoDTO;
import com.example.tarea4_grupo2.dto.RepartidorComisionMensualDTO;
import com.example.tarea4_grupo2.entity.Pedidos;
import com.example.tarea4_grupo2.entity.Repartidor;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepartidorRepository  extends JpaRepository<Repartidor, Integer> {

    Repartidor findRepartidorByIdusuariosEquals(int idusuario);

    @Query(value="select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito as restaurantedistrito, d.direccion as clientedireccion, d.distrito as clientedistrito\n" +
            "    from pedidos p\n" +
            "    inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante)\n" +
            "    inner join direcciones d on (p.direccionentrega = d.iddirecciones)\n" +
            "    where p.idrepartidor=?1", nativeQuery = true)
    List<PedidosReporteDTO> findPedidosPorRepartidor(int idRepartidor);
    //List<Pedidos> findPedidosPorRepartidor(String idRepartidor);

    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito as restaurantedistrito, d.direccion as clientedireccion, d.distrito as clientedistrito\n" +
            "    from pedidos p\n" +
            "    inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante)\n" +
            "    inner join direcciones d on (p.direccionentrega = d.iddirecciones)\n" +
            "    where d.distrito=?1 or p.restaurante_idrestaurante=?1", nativeQuery = true)
    List <PedidosReporteDTO> findReporte(String valorBuscado);

    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.nombre like CONCAT(?1,'%')",
            nativeQuery = true)
    List<PedidosReporteDTO> findPedidosByRestaurante(String nombreRestaurante);

    @Query(value = "select p.idpedidos, p.montototal, p.comisionrepartidor, p.calificacionrepartidor, r.nombre, r.distrito\n" +
            "from pedidos p\n" +
            "inner join restaurante r on (p.restaurante_idrestaurante=r.idrestaurante) where r.distrito like CONCAT(?1,'%')",
            nativeQuery = true)
    List<PedidosReporteDTO> findPedidosByDistrito(String distritoRestaurante);

    @Query(value = "SELECT sum(comisionrepartidor) as 'comision_mensual',month(fechahorapedido) as 'mes',year(fechahorapedido) as 'year'\n" +
            "FROM proyecto.pedidos \n" +
            "where (idrepartidor=?1) ",nativeQuery = true)
    List<RepartidorComisionMensualDTO> obtenerComisionPorMes(int id);

    @Query(value = "select p.idpedidos ,r.nombre as restaurante, r.distrito as distritorestaurante, d.direccion as direccioncliente, p.estadorepartidor, p.comisionrepartidor as comision, p.montototal as monto from pedidos p inner join  restaurante r on (p.restaurante_idrestaurante = r.idrestaurante) inner join direcciones d on (p.direccionentrega = d.iddirecciones) where p.estadorepartidor like concat('pendient', '%');\n", nativeQuery = true)
    List<PedidosDisponiblesDTO> findListaPedidosDisponibles();

    @Query(value = "select pe.idpedidos, pe.montototal, pe.comisionrepartidor, pe.restaurante_idrestaurante, php.cantidadplatos, pl.idplato, pl.nombre\n" +
            "from pedidos_has_plato php\n" +
            "    inner join pedidos pe on (pe.idpedidos=php.pedidos_idpedidos) inner join plato pl on (pl.idplato=php.plato_idplato)\n" +
            "    where pe.idpedidos= ?1", nativeQuery = true)
    List<PlatosPorPedidoDTO> findListaPlatosPorPedido(int id);

}
