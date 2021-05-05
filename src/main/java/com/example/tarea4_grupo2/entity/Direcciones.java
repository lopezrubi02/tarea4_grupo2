package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name = "direcciones")
public class Direcciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int iddirecciones;
    private String direccion;
    private String distrito;
    private int usuarios_idusuarios;

    public int getIddirecciones() {
        return iddirecciones;
    }

    public void setIddirecciones(int iddirecciones) {
        this.iddirecciones = iddirecciones;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public int getUsuarios_idusuarios() {
        return usuarios_idusuarios;
    }

    public void setUsuarios_idusuarios(int usuarios_idusuarios) {
        this.usuarios_idusuarios = usuarios_idusuarios;
    }
}
