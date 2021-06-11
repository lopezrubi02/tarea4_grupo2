package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="categorias")
public class Categorias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcategorias;
    @Column(nullable = false)
    private String nombrecategoria;

    @ManyToMany(mappedBy = "categoriasrestList")
    private List<Restaurante> restauranteList;

    public Integer getIdcategorias() {
        return idcategorias;
    }

    public void setIdcategorias(Integer idcategorias) {
        this.idcategorias = idcategorias;
    }

    public String getNombrecategoria() {
        return nombrecategoria;
    }

    public void setNombrecategoria(String nombrecategoria) {
        this.nombrecategoria = nombrecategoria;
    }

    public List<Restaurante> getRestauranteList() {
        return restauranteList;
    }

    public void setRestauranteList(List<Restaurante> restauranteList) {
        this.restauranteList = restauranteList;
    }
}
