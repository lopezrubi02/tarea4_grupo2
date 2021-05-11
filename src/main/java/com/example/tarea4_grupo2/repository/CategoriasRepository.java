package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Categorias;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.criteria.CriteriaBuilder;

public interface CategoriasRepository extends JpaRepository<Categorias, Integer> {
}
