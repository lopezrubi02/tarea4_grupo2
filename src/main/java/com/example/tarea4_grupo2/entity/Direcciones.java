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
    private int usuariosIdusuarios;

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

    public int getUsuariosIdusuarios() {
        return usuariosIdusuarios;
    }

    public void setUsuariosIdusuarios(int usuarios_idusuarios) {
        this.usuariosIdusuarios = usuarios_idusuarios;
    }
}
