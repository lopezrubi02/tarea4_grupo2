package com.example.tarea4_grupo2.repository;

import com.example.tarea4_grupo2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Usuario, Integer> {
}
