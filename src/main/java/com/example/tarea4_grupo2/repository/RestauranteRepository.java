package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    //Restaurante findRestauranteByIdadminrestEquals(int idAdmin);

    @Query(value = "select * from restaurante where idrestaurante = ?1", nativeQuery = true)
    Restaurante findRestauranteById(int id);

    Restaurante findRestauranteByUsuario_Idusuarios(int idAdmin);

    @Query(value = "select r.direccion from restaurante r where r.idadminrest = ?1", nativeQuery = true)
    String encontrarDireccionSegunIdRest(int idrest);

    @Query(value="select*from restaurante\n" +
            "where idadminrest=?1",nativeQuery = true)
    Restaurante obtenerperfilRest(int id);

    //Usado por adminsistema en reportes restaurante ,cambiar por group by por id(pendiente)
    @Query(value = "select r.nombre as 'restnombre', u.nombre, u.apellidos, count(p.idpedidos) as 'pedidos', (sum(p.montototal) - sum(p.comisionrepartidor) - sum(p.comisionsistema)) as 'ventastotales' from restaurante r\n" +
            "inner join usuarios u on (r.idadminrest=u.idusuarios)\n" +
            "inner join pedidos p on (r.idrestaurante=p.restaurante_idrestaurante)\n" +
            "group by r.nombre " +
            "order by r.nombre;",nativeQuery = true)
    List<RestauranteReportes_DTO> reportesRestaurantes();

    //seleccionar restaurantes por categoria
    @Query(value = "select r.idrestaurante,r.direccion,r.ruc,r.nombre,r.calificacionpromedio,r.idadminrest,r.foto, r.fotonombre, r.fotocontenttype, r.iddistrito\n" +
            " from proyecto.Restaurante r\n" +
            "inner join proyecto.Restaurante_has_categorias rc\n" +
            "on rc.restaurante_idrestaurante = r.idrestaurante where rc.categorias_idcategorias=?1",nativeQuery = true)
    List<Restaurante> listarestxcategoria (int categorias_idcategorias);
    @Query(value="select * from Restaurante where idadminrest = ?1 limit 1", nativeQuery = true)
    Optional<Restaurante> buscarRestaurantePorIdAdmin(Integer id);

    //contar cantidad de reviews dadas al restaurante
    @Query(value = "select count(idpedidos) from Pedidos where restaurante_idrestaurante=?1 and calificacionrestaurante <> 'null'",nativeQuery = true)
    Integer cantreviews(int restaurante_idrestaurante);

    // filtro restaurante por nombre
    @Query(value = "select * from Restaurante r where lower(r.nombre) like concat('%',lower(:nombre),'%')",nativeQuery = true)
    List<Restaurante> buscarRestaurantexNombre(@Param("nombre") String nombre);

    @Query(value="select ruc from Restaurante where idadminrest=?1",nativeQuery = true)
    String buscarRuc(int id);

    // filtro por precio de platos promedio
    @Query(value = "select r.*\n" +
            "from Plato p \n" +
            "inner join Restaurante r \n" +
            "on r.idrestaurante = p.restaurante_idrestaurante\n" +
            " group by p.restaurante_idrestaurante having sum(p.precio)/count(p.precio) <15",nativeQuery = true)
    List<Restaurante> listarestprecio1();

    @Query(value = "select r.*\n" +
            "from Plato p \n" +
            "inner join Restaurante r \n" +
            "on r.idrestaurante = p.restaurante_idrestaurante\n" +
            " group by p.restaurante_idrestaurante having sum(p.precio)/count(p.precio) <25 and sum(p.precio)/count(p.precio) > 15",nativeQuery = true)
    List<Restaurante> listarestprecio2();

    @Query(value = "select r.*\n" +
            "from Plato p \n" +
            "inner join Restaurante r \n" +
            "on r.idrestaurante = p.restaurante_idrestaurante\n" +
            " group by p.restaurante_idrestaurante having sum(p.precio)/count(p.precio) <40 and sum(p.precio)/count(p.precio) > 25",nativeQuery = true)
    List<Restaurante> listarestprecio3();

    @Query(value = "select r.*\n" +
            "from Plato p \n" +
            "inner join Restaurante r \n" +
            "on r.idrestaurante = p.restaurante_idrestaurante\n" +
            "group by p.restaurante_idrestaurante having sum(p.precio)/count(p.precio) > 40",nativeQuery = true)
    List<Restaurante> listarestprecio4();

    @Query(value = "select * from Restaurante r \n" +
            "where round(calificacionpromedio) = ?1",nativeQuery = true)
    List<Restaurante> listarestcalificacion(int numestrellas);

}