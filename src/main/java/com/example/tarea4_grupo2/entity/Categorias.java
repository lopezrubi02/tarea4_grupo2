package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name = "categorias")
public class Categorias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcategorias;
    private String nombrecategoria;

    public int getIdcategorias() {
        return idcategorias;
    }

    public void setIdcategorias(int idcategorias) {
        this.idcategorias = idcategorias;
    }

    public String getNombrecategoria() {
        return nombrecategoria;
    }

    public void setNombrecategoria(String nombrecategoria) {
        this.nombrecategoria = nombrecategoria;
    }
}
