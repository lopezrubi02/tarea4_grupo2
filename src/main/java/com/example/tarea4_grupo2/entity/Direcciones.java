package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name = "direcciones")
public class Direcciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int iddirecciones;
    private String direccion;
//    private String distrito;
    private int activo;

    @ManyToOne
    @JoinColumn(name = "iddistrito")
    private Distritos distrito;

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

    public Distritos getDistrito() {
        return distrito;
    }

    public void setDistrito(Distritos distrito) {
        this.distrito = distrito;
    }

    public int getUsuariosIdusuarios() {
        return usuariosIdusuarios;
    }

    public void setUsuariosIdusuarios(int usuarios_idusuarios) {
        this.usuariosIdusuarios = usuarios_idusuarios;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }
}
