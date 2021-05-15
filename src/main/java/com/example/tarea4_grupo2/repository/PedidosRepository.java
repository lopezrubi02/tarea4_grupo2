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

}
