package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.TiempoMedio_ClienteDTO;
import com.example.tarea4_grupo2.dto.Top3Restaurantes_ClienteDTO;
import com.example.tarea4_grupo2.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidosRepository extends JpaRepository<Pedidos, Integer> {
    @Query(value = "select re.nombre as restaurante, count(*) as veces_asistida from proyecto.pedidos pe inner join proyecto.restaurante re on (re.idrestaurante = pe.restaurante_idrestaurante) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc limit 0, 3;", nativeQuery = true)
    List<Top3Restaurantes_ClienteDTO> obtenerTop3Restaurantes(int idcliente, int anio, int mes);

    //@Query(value = "select sum((pepla.cantidadplatos * p.precio) - pe.montototal) as diferencia\n" +
    //        "from proyecto.pedidos pe \n" +
    //        "inner join proyecto.pedidos_has_plato pepla on (pepla.pedidos_idpedidos = pe.idpedidos)\n" +
    //        "inner join proyecto.plato p on (pepla.plato_idplato = p.idplato)\n" +
    //        "where pe.idcliente = 8 and year(pe.fechahorapedido) = 2021;", nativeQuery = true);
    //public int dineroAhorrado(int idcliente, int anio);

    @Query(value = "SELECT re.nombre as nombre_restaurante, avg(pe.tiempodelivery) as tiempo_promedio FROM proyecto.pedidos pe inner join proyecto.restaurante re on (re.idrestaurante = pe.restaurante_idrestaurante) where pe.idcliente = ?1 and year(pe.fechahorapedido) = ?2 and month(pe.fechahorapedido) = ?3 group by re.idrestaurante order by count(*) desc; ", nativeQuery = true)
    List<TiempoMedio_ClienteDTO> obtenerTiemposPromedio(int idcliente, int anio, int mes);
}
