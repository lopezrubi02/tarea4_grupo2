package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.dto.RestauranteReportes_DTO;
import com.example.tarea4_grupo2.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    Restaurante findRestauranteByIdadminrestEquals(int idAdmin);


    //Usado por adminsistema en reportes restaurante
    @Query(value = "select r.nombre as 'restnombre', u.nombre, u.apellidos, count(p.idpedidos) as 'pedidos', (sum(p.montototal) - sum(p.comisionrepartidor) - sum(p.comisionsistema)) as 'ventastotales' from restaurante r\n" +
            "inner join usuarios u on (r.idadminrest=u.idusuarios)\n" +
            "inner join pedidos p on (r.idrestaurante=p.restaurante_idrestaurante)\n" +
            "order by 'restnombre';",nativeQuery = true)
    List<RestauranteReportes_DTO> reportesRestaurantes();



}
