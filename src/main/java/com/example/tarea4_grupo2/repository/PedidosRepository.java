package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.*;
import com.example.tarea4_grupo2.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PedidosRepository extends JpaRepository<Pedidos, Integer> {
/* Obtencion del Top 3 de Restaurantes*/
    @Query(value = "select re.nombre as restaurante, count(*) as vecesasistida from " +
            "proyecto.Pedidos pe inner join proyecto.Restaurante re " +
            "on (re.idrestaurante = pe.restauranteIdrestaurante) where pe.idcliente = ?1 and " +
            "year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc limit 0, 3", nativeQuery = true)
    List<Top3Restaurantes_ClienteDTO> obtenerTop3Restaurantes(int idcliente, int anio, int mes);
/*Este es para hallar el dinero ahorrado*/
    @Query(value = "select sum((pepla.cantidadplatos * p.precio) - pe.montototal) as diferencia from proyecto.Pedidos pe " +
            "inner join proyecto.Pedidoshasplato pepla on (pepla.pedidosIdpedidos = pe.idpedidos) " +
            "inner join proyecto.Plato p on (pepla.platoIdplato = p.idplato) where " +
            "pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3", nativeQuery = true)
    DineroAhorrado_ClienteDTO dineroAhorrado(int idcliente, int anio, int mes);
/*Obtención del Top 3 de Platos*/
    @Query(value = "select p.nombre as nombreplato, count(*) as vecespedido \n" +
            "from proyecto.Pedidos pe \n" +
            "inner join proyecto.Pedidoshasplato pepla on (pepla.pedidosIdpedidos = pe.idpedidos)\n" +
            "inner join proyecto.Plato p on (pepla.platoIdplato = p.idplato)\n" +
            "where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2\n" +
            "and month(pe.fechahorapedido) = ?3 group by p.idplato order by count(*) desc limit 0, 3", nativeQuery = true)
    List<Top3Platos_ClientesDTO> obtenerTop3Platos(int idcliente, int anio, int mes);

    /*Halla el historial de consumo*/
    @Query(value = "select re.nombre as nomrestaurante, count(*) as asistencia, sum(pe.montototal) as consumomensual\n" +
            "from proyecto.Pedidos pe\n" +
            "inner join proyecto.Restaurante re on (re.idrestaurante = pe.restauranteIdrestaurante)\n" +
            "where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2\n" +
            "and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc limit 0, 3 ", nativeQuery = true)
    List<HistorialConsumo_ClienteDTO> obtenerHistorialConsumo(int idcliente, int anio, int mes);

    /*Halla el tiempo promedio de delivery*/
    @Query(value = "SELECT re.nombre as nombrerestaurante, avg(pe.tiempodelivery) as tiempopromedio " +
            "FROM proyecto.Pedidos pe inner join proyecto.Restaurante re on " +
            "(re.idrestaurante = pe.restauranteIdrestaurante) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 " +
            "and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc", nativeQuery = true)
    List<TiempoMedio_ClienteDTO> obtenerTiemposPromedio(int idcliente, int anio, int mes);

    /******ADMINISTRADOR SISTEMA**********/
    /*Reporte De Delivery pedidos para adminsistema*/
    @Query(value = "select date(fechahorapedido) as 'fecha',count(idpedidos) as 'pedidos',sum(comisionsistema) as 'comision' from Pedidos\n" +
            "where Pedidos.fechahorapedido IS NOT NULL group by YEAR(fechahoraentregado),MONTH(fechahoraentregado),DAY(fechahoraentregado)\n" +
            "ORDER BY CONCAT(SUBSTRING_INDEX(fecha , '/', -1),SUBSTRING_INDEX(SUBSTRING_INDEX(fecha , '/', 2), '/', -1),SUBSTRING_INDEX(fecha , '/', 1)) DESC;\n" +
            "\n",nativeQuery = true)
    List<DeliveryReportes_DTO> reportesDelivery();

    @Query(value = "select date(fechahorapedido) as 'fecha',count(idpedidos) as 'pedidos',sum(comisionsistema) as 'comision' from Pedidos\n" +
            "where Pedidos.fechahorapedido IS NOT NULL and year(Pedidos.fechahorapedido)= ?1 and month(Pedidos.fechahorapedido) =?2 group by YEAR(fechahoraentregado),MONTH(fechahoraentregado),DAY(fechahoraentregado)\n" +
            "ORDER BY CONCAT(SUBSTRING_INDEX(fecha , '/', -1),SUBSTRING_INDEX(SUBSTRING_INDEX(fecha , '/', 2), '/', -1),SUBSTRING_INDEX(fecha , '/', 1)) DESC;\n" +
            "\n",nativeQuery = true)
    List<DeliveryReportes_DTO> reportesDeliveryFecha(int anio, int mes);


    @Query(value = "select date(fechahorapedido) as 'fecha'from Pedidos\n" +
            "WHERE Pedidos.fechahorapedido IS NOT NULL \n" +
            "group by YEAR(fechahoraentregado),MONTH(fechahoraentregado),DAY(fechahoraentregado)\n" +
            "ORDER BY CONCAT(SUBSTRING_INDEX(fecha , '/', -1),SUBSTRING_INDEX(SUBSTRING_INDEX(fecha , '/', 2), '/', -1),SUBSTRING_INDEX(fecha , '/', 1)) ASC limit 1;\n",
            nativeQuery = true)
    String primerPedido();

    @Query(value = "SELECT b.Days as 'fecha', count(Pedidos.idpedidos) as 'pedidos', ifnull(sum(Pedidos.comisionsistema),0) as 'comision' FROM \n" +
            "    (SELECT a.Days \n" +
            "    FROM (\n" +
            "        SELECT curdate() - INTERVAL (a.a + (10 * b.a) + (100 * c.a)) DAY AS Days\n" +
            "        FROM       (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS a\n" +
            "        CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS b\n" +
            "        CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS c\n" +
            "    ) a\n" +
            "WHERE a.Days >= curdate() -  INTERVAL (SELECT TIMESTAMPDIFF(DAY,?1 , curdate())) DAY)  b\n" +
            "LEFT JOIN Pedidos\n" +
            "ON date(Pedidos.fechahoraentregado) = b.Days\n" +
            "group by YEAR(b.Days),MONTH(b.Days),DAY(b.Days)\n" +
            "ORDER BY CONCAT(SUBSTRING_INDEX(b.Days , '/', -1),SUBSTRING_INDEX(SUBSTRING_INDEX(b.Days , '/', 2), '/', -1),SUBSTRING_INDEX(b.Days , '/', 1)) DESC;",
            nativeQuery = true)
    List<DeliveryReportes_DTO> reportesDelivery2(String fecha);


    /******ADMINISTRADOR RESTAURANTE**********/

    @Query(value = "select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,dr.nombredistrito \n" +
            "from Pedidos p\n" +
            "inner join Usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join Restaurante r on p.restauranteIdrestaurante = r.idrestaurante\n" +
            "inner join Direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "inner join Distritos dr on dr.iddistritos = d.iddistrito\n" +
            "where r.idrestaurante=?1 and p.estadorestaurante='pendiente'",nativeQuery = true)
    List<PedidosAdminRestDto> listaPedidos(Integer id);

    @Query(value = "select p.idpedidos as numeropedido, p.fechahorapedido as fechahorapedido, \n" +
            "u.nombre as nombre, u.apellidos as apellidos, p.montototal as montototal, pt.nombre as nombreplato,\n" +
            "m.metodo as metodo, d.nombredistrito as distrito\n" +
            " from Pedidos p\n" +
            " inner join Usuarios u on u.idusuarios = p.idcliente \n" +
            " inner join Direcciones dir on dir.iddirecciones = p.direccionentrega\n" +
            "  inner join Distritos d on d.iddistritos = dir.iddistrito\n" +
            " inner join Metodospago m on m.idmetodospago = p.idmetodopago\n" +
            " inner join Pedidoshasplato pl on pl.pedidosIdpedidos = p.idpedidos\n" +
            " inner join Plato pt on pt.idplato = pl.platoIdplato\n" +
            " where p.estadorestaurante = 'entregado' and p.restauranteIdrestaurante = ?1\n" +
            "  order by p.fechahorapedido asc", nativeQuery = true)
    List<PedidosReporteDto> listaPedidosReporteporFechamasantigua(Integer id);

    @Query(value = "select p.idpedidos as numeropedido, p.fechahorapedido as fechahorapedido, \n" +
            "u.nombre as nombre, u.apellidos as apellidos, p.montototal as montototal, pt.nombre as nombreplato,\n" +
            "m.metodo as metodo, d.nombredistrito as distrito\n" +
            "from Pedidos p\n" +
            "inner join Usuarios u on u.idusuarios = p.idcliente \n" +
            "inner join Direcciones dir on dir.iddirecciones = p.direccionentrega\n" +
            "inner join Distritos d on d.iddistritos = dir.iddistrito\n" +
            "inner join Metodospago m on m.idmetodospago = p.idmetodopago\n" +
            "inner join Pedidoshasplato pl on pl.pedidosIdpedidos = p.idpedidos\n" +
            "inner join Plato pt on pt.idplato = pl.platoIdplato\n" +
            "where p.estadorestaurante = 'entregado' and p.restauranteIdrestaurante = ?2\n" +
            "and (p.idpedidos like %?1% or p.fechahorapedido like %?1%\n" +
            "or u.nombre like %?1% or u.apellidos like %?1%\n" +
            "or p.montototal like %?1% or pt.nombre like %?1%\n" +
            "or m.metodo like %?1% or d.nombredistrito like %?1%)\n" +
            "order by p.fechahorapedido asc", nativeQuery = true)
    List<PedidosReporteDto> buscarPorReporte(String name, Integer id);

    @Query(value = "select \n" +
            "MONTHNAME(p.fechahorapedido) as mes,\n" +
            "YEAR(p.fechahorapedido) as anio,\n" +
            "sum(p.montototal) as ganancia from Pedidos p where p.restauranteIdrestaurante = ?1\n" +
            "group by MONTHNAME(p.fechahorapedido)", nativeQuery = true)
    List<PedidosGananciaMesDto> gananciaPorMes(Integer id);

    @Query(value="select pt.nombre as nombreplato,\n" +
            "count(pl.platoIdplato) as cantidad,\n" +
            "sum(p.montototal) as ganancia\n" +
            "from Plato pt\n" +
            "inner join Pedidoshasplato pl on pl.platoIdplato = pt.idplato\n" +
            "inner join Pedidos p on p.idpedidos = pl.pedidosIdpedidos\n" +
            "where p.restauranteIdrestaurante = ?1\n" +
            "group by pt.nombre order by cantidad desc limit 5",nativeQuery = true)
    List<PedidosTop5Dto> platosMasVendidos(Integer id);

    @Query(value="select pt.nombre as nombreplato,\n" +
            "count(pl.platoIdplato) as cantidad,\n" +
            "sum(p.montototal) as ganancia\n" +
            "from Plato pt\n" +
            "inner join Pedidoshasplato pl on pl.platoIdplato = pt.idplato\n" +
            "inner join Pedidos p on p.idpedidos = pl.pedidosIdpedidos\n" +
            "where p.restauranteIdrestaurante = ?1\n" +
            "group by pt.nombre order by cantidad asc limit 5;",nativeQuery = true)
    List<PedidosTop5Dto> platosMenosVendidos(Integer id);

    @Query(value = "select \n" +
            "p.calificacionrestaurante as calificacion,\n" +
            "p.comentario as comentario\n" +
            "from Pedidos p where p.restauranteIdrestaurante = ?1 and p.estadorestaurante = 'entregado'\n" +
            "order by p.calificacionrestaurante desc", nativeQuery = true)
    List<ComentariosDto>comentariosUsuarios(Integer id);

    @Query(value = "select \n" +
            "p.calificacionrestaurante as calificacion,\n" +
            "p.comentario as comentario\n" +
            "from Pedidos p where p.restauranteIdrestaurante = ?2 and p.estadorestaurante = 'entregado'\n" +
            "and p.calificacionrestaurante like %?1%\n" +
            "order by p.calificacionrestaurante desc", nativeQuery = true)
    List<ComentariosDto>buscarComentariosUsuarios(String name, Integer id);

    @Query(value="select \n" +
            "avg(p.calificacionrestaurante) as calificacionpromedio\n" +
            "from Pedidos p where p.restauranteIdrestaurante = ?1 and p.estadorestaurante = 'entregado'", nativeQuery = true)
    BigDecimal calificacionPromedio(Integer id);

    @Query(value="select p.idpedidos as idpedidos,\n" +
            "pl.nombre as nombre,\n" +
            "php.descripcion as descripcion,\n" +
            "php.cantidadplatos as cantidadplatos,\n" +
            "php.cubiertos as cubiertos,\n" +
            "d.direccion as direccion,\n" +
            "dr.nombredistrito as nombredistrito,\n" +
            "pl.precio as precio from Pedidos p\n" +
            "inner join Pedidoshasplato php on p.idpedidos = php.pedidosIdpedidos\n" +
            "inner join Plato pl on php.platoIdplato = pl.idplato\n" +
            "inner join Direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "inner join Distritos dr on dr.iddistritos = d.iddistrito\n" +
            "where p.idpedidos=?1",nativeQuery = true)
    List<PedidoDetallesDto>detallepedidos(Integer id);

    @Query(value="select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,dr.nombredistrito from Pedidos p\n" +
            "inner join Usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join Restaurante r on p.restauranteIdrestaurante = r.idrestaurante\n" +
            "inner join Direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "inner join Distritos dr on dr.iddistritos = d.iddistrito\n" +
            "where r.idrestaurante=?1 and p.estadorestaurante='aceptado'",nativeQuery = true)
    List<PedidoAceptadosDtos>aceptadopedidos(Integer id);

    @Query(value="select p.idpedidos,p.montototal,concat(u.nombre,' ',u.apellidos)cliente,cast(p.fechahorapedido as DATE)fechahorapedido,d.direccion,dr.nombredistrito from Pedidos p\n" +
            "inner join Usuarios u on p.idcliente = u.idusuarios\n" +
            "inner join Restaurante r on p.restauranteIdrestaurante = r.idrestaurante\n" +
            "inner join Direcciones d on p.direccionentrega = d.iddirecciones\n" +
            "inner join Distritos dr on dr.iddistritos = d.iddistrito\n" +
            "where r.idrestaurante=?1 and p.estadorestaurante='preparado'",nativeQuery = true)
    List<PedidosPreparadosDto>preparadopedidos(Integer id);

    @Query(value = "select * from Pedidos where idcliente=?1 and restauranteIdrestaurante=?2",nativeQuery = true)
    List<Pedidos> listapedidoxcliente (int idcliente,int idrestaurante);

    @Query(value = "select * from Pedidos where idcliente=?1 and restauranteIdrestaurante= ?2 and montototal='0'",nativeQuery = true)
    Pedidos pedidoencursoxrestaurante(int idcliente, int restauranteIdrestaurante);

    @Query(value = "select * from Pedidos where idcliente=?1 and montototal='0'",nativeQuery = true)
    List<Pedidos> listapedidospendientes(int idcliente);

    @Query(value = "select * from Pedidos where idcliente=?1 and montototal!='0'",nativeQuery = true)
    List<Pedidos> pedidosfinxcliente(int idcliente);

    @Query(value = "select * from Pedidos where idcliente=?1 and montototal!='0' and estadorestaurante='cancelado' and estadorepartidor='pendiente'",nativeQuery = true)
    List<Pedidos> listapedidoscanceladosxrest(int idcliente);

    List<Pedidos> findAllByIdclienteEquals(int idcliente);

}
