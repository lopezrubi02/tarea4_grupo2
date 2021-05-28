package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.FotosPlatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface FotosPlatosRepository extends JpaRepository<FotosPlatos,Integer> {

    @Query(value="select * from fotosplatos fp where fp.idplato = ?1 limit 1", nativeQuery = true)
    Optional<FotosPlatos> encontrarIdPlato(int idplato);

    @Query(value="select * from fotosplatos fp where fp.idplato = ?1 limit 1", nativeQuery = true)
    Optional<FotosPlatos> fotoplatoxidplato(int idplato);

}
