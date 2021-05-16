package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    //Restaurante findRestauranteByIdadminrestEquals(int idAdmin);

    @Query(value="select*from restaurante\n" +
            "where idadminrest=?1",nativeQuery = true)
    Restaurante obtenerperfilRest(int id);

    //Usado por adminsistema en reportes restaurante
    @Query(value = "select r.nombre as 'restnombre', u.nombre, u.apellidos, count(p.idpedidos) as 'pedidos', (sum(p.montototal) - sum(p.comisionrepartidor) - sum(p.comisionsistema)) as 'ventastotales' from restaurante r\n" +
            "inner join usuarios u on (r.idadminrest=u.idusuarios)\n" +
            "inner join pedidos p on (r.idrestaurante=p.restaurante_idrestaurante)\n" +
            "order by 'restnombre';",nativeQuery = true)
    List<RestauranteReportes_DTO> reportesRestaurantes();

    //seleccionar restaurantes por categoria
    @Query(value = "select r.idrestaurante,r.direccion,r.ruc,r.nombre,r.calificacionpromedio,r.idadminrest,r.foto, r.fotonombre, r.fotocontenttype, r.iddistrito\n" +
            " from proyecto.restaurante r\n" +
            "inner join proyecto.restaurante_has_categorias rc\n" +
            "on rc.restaurante_idrestaurante = r.idrestaurante where rc.categorias_idcategorias=?1",nativeQuery = true)
    List<Restaurante> listarestxcategoria (int categorias_idcategorias);

    //contar cantidad de reviews dadas al restaurante
    @Query(value = "select count(idpedidos) from pedidos where restaurante_idrestaurante=?1 and calificacionrestaurante <> 'null'",nativeQuery = true)
    Integer cantreviews(int restaurante_idrestaurante);

    @Query(value = "select * from restaurante r where lower(r.nombre) like lower('%?1%')",nativeQuery = true)
    List<Restaurante> buscarRestaurantexNombre(String nombre);

}
