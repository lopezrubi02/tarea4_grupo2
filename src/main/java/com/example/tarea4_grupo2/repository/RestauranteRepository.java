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
    @Query(value = "select r.nombre as 'restnombre', u.nombre, u.apellidos, count(p.idpedidos) as 'pedidos', (sum(p.montototal) - sum(p.comisionrepartidor) - sum(p.comisionsistema)) " +
            "as 'ventastotales' from restaurante r\n" +
            "inner join usuarios u on (r.idadminrest=u.idusuarios)\n" +
            "inner join pedidos p on (r.idrestaurante=p.restauranteidrestaurante)\n" +
            "group by r.nombre " +
            "order by r.nombre;",nativeQuery = true)
    List<RestauranteReportes_DTO> reportesRestaurantes();

    @Query(value = "select r.nombre as 'restnombre', u.nombre, u.apellidos, count(p.idpedidos) as 'pedidos', (sum(p.montototal) - sum(p.comisionrepartidor) - sum(p.comisionsistema)) " +
            "as 'ventastotales' from restaurante r\n" +
            "inner join usuarios u on (r.idadminrest=u.idusuarios)\n" +
            "inner join pedidos p on (r.idrestaurante=p.restauranteidrestaurante)\n" +
            "where r.nombre like ?1 group by r.nombre " +
            "order by r.nombre;",nativeQuery = true)
    List<RestauranteReportes_DTO> reportesRestaurantes2(String buscar);

    //seleccionar restaurantes por categoria
    @Query(value = "select r.idrestaurante,r.direccion,r.ruc,r.nombre,r.calificacionpromedio,r.idadminrest,r.foto, r.fotonombre, r.fotocontenttype, r.iddistrito\n" +
            " from proyecto.restaurante r\n" +
            "inner join proyecto.restaurantehascategorias rc\n" +
            "on rc.restaurantesidrestaurantes = r.idrestaurante where rc.categoriasidcategorias=?1",nativeQuery = true)
    List<Restaurante> listarestxcategoria (int categoriasidcategorias);
    @Query(value="select * from restaurante where idadminrest = ?1 limit 1", nativeQuery = true)
    Optional<Restaurante> buscarRestaurantePorIdAdmin(Integer id);

    //contar cantidad de reviews dadas al restaurante
    @Query(value = "select count(idpedidos) from pedidos where restauranteidrestaurante=?1 and calificacionrestaurante <> 'null'",nativeQuery = true)
    Integer cantreviews(int restaurante_idrestaurante);

    // filtro restaurante por nombre
    @Query(value = "select r.* from restaurante r " +
            "inner join distritos d \n" +
            "on d.iddistritos = r.iddistrito\n" +
            "where d.nombredistrito = :distrito " +
            "and lower(r.nombre) like concat('%',lower(:nombre),'%')",nativeQuery = true)
    List<Restaurante> buscarRestaurantexNombre(@Param("distrito") String distrito, @Param("nombre") String nombre);

    @Query(value="select ruc from restaurante where idadminrest=?1",nativeQuery = true)
    String buscarRuc(int id);

    Optional<Restaurante> findRestauranteByRuc(String ruc);

    // filtro por precio de platos promedio
    @Query(value = "select r.*\n" +
            "from plato p \n" +
            "inner join restaurante r \n" +
            "on r.idrestaurante = p.restauranteidrestaurante\n" +
            "inner join distritos d\n" +
            "            on d.iddistritos = r.iddistrito\n" +
            "            where d.nombredistrito = ?1 " +
            " group by p.restauranteidrestaurante having sum(p.precio)/count(p.precio) <=15",nativeQuery = true)
    List<Restaurante> listarestprecio1(String nombredistrito);

    @Query(value = "select r.*\n" +
            "from plato p \n" +
            "inner join restaurante r \n" +
            "on r.idrestaurante = p.restauranteidrestaurante\n" +
            "inner join distritos d\n" +
            "            on d.iddistritos = r.iddistrito\n" +
            "            where d.nombredistrito = ?1 " +
            " group by p.restauranteidrestaurante having sum(p.precio)/count(p.precio) <=25 and sum(p.precio)/count(p.precio) > 15",nativeQuery = true)
    List<Restaurante> listarestprecio2(String nombredistrito);

    @Query(value = "select r.*\n" +
            "from plato p \n" +
            "inner join restaurante r \n" +
            "on r.idrestaurante = p.restauranteidrestaurante\n" +
            "inner join distritos d\n" +
            "            on d.iddistritos = r.iddistrito\n" +
            "            where d.nombredistrito = ?1 " +
            " group by p.restauranteidrestaurante having sum(p.precio)/count(p.precio) <=40 and sum(p.precio)/count(p.precio) > 25",nativeQuery = true)
    List<Restaurante> listarestprecio3(String nombredistrito);

    @Query(value = "select r.*\n" +
            "from plato p \n" +
            "inner join restaurante r \n" +
            "on r.idrestaurante = p.restauranteidrestaurante\n" +
            "inner join distritos d\n" +
            "            on d.iddistritos = r.iddistrito\n" +
            "            where d.nombredistrito = ?1 " +
            "group by p.restauranteidrestaurante having sum(p.precio)/count(p.precio) > 40",nativeQuery = true)
    List<Restaurante> listarestprecio4(String nombredistrito);

    @Query(value = "select * from restaurante r \n" +
            "where round(calificacionpromedio) = ?1",nativeQuery = true)
    List<Restaurante> listarestcalificacion(int numestrellas);

    @Query(value= "select count(p.calificacionrestaurante) from pedidos p\n" +
            "where p.calificacionrestaurante is not null and p.estadorestaurante = 'entregado' and p.restauranteidrestaurante=?1", nativeQuery = true)
    Integer obtenerCantidadCalificaciones(int idrestaurante);

    /*TODO en cliente: mandar lista de restaurantes de acuerdo a un distrito */
    @Query(value = "select r.* from restaurante r\n" +
            "inner join distritos d\n" +
            "on d.iddistritos = r.iddistrito\n" +
            "where lower(d.nombredistrito) = lower(?1);", nativeQuery = true)
    List<Restaurante> listarestaurantesxdistrito (String distrito);


}