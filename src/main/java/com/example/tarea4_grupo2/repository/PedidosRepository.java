package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidosRepository extends JpaRepository<Pedidos, Integer> {
/* Obtencion del Top 3 de Restaurantes*/
    @Query(value = "select re.nombre as restaurante, count(*) as vecesasistida from proyecto.pedidos pe inner join proyecto.restaurante re on (re.idrestaurante = pe.restaurante_idrestaurante) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc limit 0, 3", nativeQuery = true)
    List<Top3Restaurantes_ClienteDTO> obtenerTop3Restaurantes(int idcliente, int anio, int mes);
/*Este es para hallar el dinero ahorrado*/
    @Query(value = "select sum((pepla.cantidadplatos * p.precio) - pe.montototal) as diferencia from proyecto.pedidos pe inner join proyecto.pedidos_has_plato pepla on (pepla.pedidos_idpedidos = pe.idpedidos) inner join proyecto.plato p on (pepla.plato_idplato = p.idplato) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3", nativeQuery = true)
    DineroAhorrado_ClienteDTO dineroAhorrado(int idcliente, int anio, int mes);
/*Obtención del Top 3 de Platos*/
    @Query(value = "select p.nombre as nombreplato, count(*) as vecespedido \n" +
            "from proyecto.pedidos pe \n" +
            "inner join proyecto.pedidos_has_plato pepla on (pepla.pedidos_idpedidos = pe.idpedidos)\n" +
            "inner join proyecto.plato p on (pepla.plato_idplato = p.idplato)\n" +
            "where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2\n" +
            "and month(pe.fechahorapedido) = ?3 group by p.idplato order by count(*) desc limit 0, 3", nativeQuery = true)
    List<Top3Platos_ClientesDTO> obtenerTop3Platos(int idcliente, int anio, int mes);

    /*Halla el historial de consumo*/
    @Query(value = "select re.nombre as nomrestaurante, count(*) as asistencia, sum(pe.montototal) as consumomensual\n" +
            "from proyecto.pedidos pe\n" +
            "inner join proyecto.restaurante re on (re.idrestaurante = pe.restaurante_idrestaurante)\n" +
            "where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2\n" +
            "and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc limit 0, 3 ", nativeQuery = true)
    List<HistorialConsumo_ClienteDTO> obtenerHistorialConsumo(int idcliente, int anio, int mes);

    /*Halla el tiempo promedio de delivery*/
    @Query(value = "SELECT re.nombre as nombrerestaurante, avg(pe.tiempodelivery) as tiempopromedio FROM proyecto.pedidos pe inner join proyecto.restaurante re on (re.idrestaurante = pe.restaurante_idrestaurante) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc", nativeQuery = true)
    List<TiempoMedio_ClienteDTO> obtenerTiemposPromedio(int idcliente, int anio, int mes);

    /*Reporte De Delivery pedidos para adminsistema*/
    @Query(value = "select date(fechahorapedido) as 'fecha',count(idpedidos) as 'pedidos',sum(comisionsistema) as 'comision' from pedidos\n" +
            "group by YEAR(fechahoraentregado),MONTH(fechahoraentregado),DAY(fechahoraentregado)\n" +
            "ORDER BY CONCAT(SUBSTRING_INDEX(fecha , '/', -1),SUBSTRING_INDEX(SUBSTRING_INDEX(fecha , '/', 2), '/', -1),SUBSTRING_INDEX(fecha , '/', 1)) DESC;\n" +
            "\n",nativeQuery = true)
    List<DeliveryReportes_DTO> reportesDelivery();


    /******ADMINISTRADOR RESTAURANTE**********/

    @Query(value = "select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,d.distrito from pedidos p\n" +
            "inner join usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join restaurante r on p.restaurante_idrestaurante = r.idrestaurante\n" +
            "inner join direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "where r.idrestaurante=?1 and p.estadorestaurante='pendiente'",nativeQuery = true)
    List<PedidosAdminRestDto> listaPedidos(Integer id);

    @Query(value = "select p.idpedidos as numeropedido, p.fechahorapedido as fechahorapedido, \n" +
            "u.nombre as nombre, u.apellidos as apellidos, p.montototal as montototal, pt.nombre as nombreplato,\n" +
            "m.metodo as metodo, d.nombredistrito as distrito\n" +
            " from pedidos p\n" +
            " inner join usuarios u on u.idusuarios = p.idcliente \n" +
            " inner join direcciones dir on dir.iddirecciones = p.direccionentrega\n" +
            "  inner join distritos d on d.iddistritos = dir.iddistrito\n" +
            " inner join metodospago m on m.idmetodospago = p.idmetodopago\n" +
            " inner join pedidos_has_plato pl on pl.pedidos_idpedidos = p.idpedidos\n" +
            " inner join plato pt on pt.idplato = pl.plato_idplato\n" +
            " where p.estadorestaurante = 'entregado' and p.restaurante_idrestaurante = ?1\n" +
            "  order by p.fechahorapedido asc", nativeQuery = true)
    List<PedidosReporteDTO> listaPedidosReporteporFechamasantigua(Integer id);

    @Query(value = "select p.idpedidos as numeropedido, p.fechahorapedido as fechahorapedido, \n" +
            "u.nombre as nombre, u.apellidos as apellidos, p.montototal as montototal, pt.nombre as nombreplato,\n" +
            "m.metodo as metodo, d.nombredistrito as distrito\n" +
            "from pedidos p\n" +
            "inner join usuarios u on u.idusuarios = p.idcliente \n" +
            "inner join direcciones dir on dir.iddirecciones = p.direccionentrega\n" +
            "inner join distritos d on d.iddistritos = dir.iddistrito\n" +
            "inner join metodospago m on m.idmetodospago = p.idmetodopago\n" +
            "inner join pedidos_has_plato pl on pl.pedidos_idpedidos = p.idpedidos\n" +
            "inner join plato pt on pt.idplato = pl.plato_idplato\n" +
            "where p.estadorestaurante = 'entregado' and p.restaurante_idrestaurante = ?2\n" +
            "and (p.idpedidos like %?1% or p.fechahorapedido like %?1%\n" +
            "or u.nombre like %?1% or u.apellidos like %?1%\n" +
            "or p.montototal like %?1% or pt.nombre like %?1%\n" +
            "or m.metodo like %?1% or d.nombredistrito like %?1%)\n" +
            "order by p.fechahorapedido asc", nativeQuery = true)
    List<PedidosReporteDTO> buscarPorReporte(String name, Integer id);

    @Query(value = "select \n" +
            "MONTHNAME(p.fechahorapedido) as mes,\n" +
            "YEAR(p.fechahorapedido) as anio,\n" +
            "sum(p.montototal) as ganancia from pedidos p where p.restaurante_idrestaurante = ?1\n" +
            "group by MONTHNAME(p.fechahorapedido)", nativeQuery = true)
    List<PedidosGananciaMesDto> gananciaPorMes(Integer id);

    @Query(value="select pt.nombre as nombreplato,\n" +
            "count(pl.plato_idplato) as cantidad,\n" +
            "sum(p.montototal) as ganancia\n" +
            "from plato pt\n" +
            "inner join pedidos_has_plato pl on pl.plato_idplato = pt.idplato\n" +
            "inner join pedidos p on p.idpedidos = pl.pedidos_idpedidos\n" +
            "where p.restaurante_idrestaurante = ?1\n" +
            "group by pt.nombre order by cantidad desc limit 5",nativeQuery = true)
    List<PedidosTop5Dto> platosMasVendidos(Integer id);

    @Query(value="select pt.nombre as nombreplato,\n" +
            "count(pl.plato_idplato) as cantidad,\n" +
            "sum(p.montototal) as ganancia\n" +
            "from plato pt\n" +
            "inner join pedidos_has_plato pl on pl.plato_idplato = pt.idplato\n" +
            "inner join pedidos p on p.idpedidos = pl.pedidos_idpedidos\n" +
            "where p.restaurante_idrestaurante = ?1\n" +
            "group by pt.nombre order by cantidad asc limit 5;",nativeQuery = true)
    List<PedidosTop5Dto> platosMenosVendidos(Integer id);

    @Query(value = "select \n" +
            "p.calificacionrestaurante as calificacion,\n" +
            "p.comentario as comentario\n" +
            "from pedidos p where p.restaurante_idrestaurante = ?1\n" +
            "order by p.calificacionrestaurante desc", nativeQuery = true)
    List<ComentariosDto>comentariosUsuarios(Integer id);

    @Query(value = "select \n" +
            "p.calificacionrestaurante as calificacion,\n" +
            "p.comentario as comentario\n" +
            "from pedidos p where p.restaurante_idrestaurante = ?2\n" +
            "and p.calificacionrestaurante like %?1%\n" +
            "order by p.calificacionrestaurante desc", nativeQuery = true)
    List<ComentariosDto>buscarComentariosUsuarios(String name, Integer id);

    @Query(value="select \n" +
            "avg(p.calificacionrestaurante) as calificacionpromedio\n" +
            "from pedidos p where p.restaurante_idrestaurante = ?1", nativeQuery = true)
    List<AvgCalifDto>calificacionPromedio(Integer id);

    @Query(value="select p.idpedidos,pl.nombre,php.descripcion,php.cantidadplatos,php.cubiertos,d.direccion,d.distrito,p.montototal from pedidos p\n" +
            "inner join pedidos_has_plato php on p.idpedidos = php.pedidos_idpedidos\n" +
            "inner join plato pl on php.plato_idplato = pl.idplato\n" +
            "inner join direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "where p.idpedidos=?1",nativeQuery = true)
    List<PedidoDetallesDto>detallepedidos(Integer id);

    @Query(value="select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,d.distrito from pedidos p\n" +
            "inner join usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join restaurante r on p.restaurante_idrestaurante = r.idrestaurante\n" +
            "inner join direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "where r.idrestaurante=2 and p.estadorestaurante='aceptado'",nativeQuery = true)
    List<PedidoAceptadosDtos>aceptadopedidos();

    @Query(value="select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,d.distrito from pedidos p\n" +
            "inner join usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join restaurante r on p.restaurante_idrestaurante = r.idrestaurante\n" +
            "inner join direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "where r.idrestaurante=2 and p.estadorestaurante='preparado'",nativeQuery = true)
    List<PedidosPreparadosDto>preparadopedidos();
}
