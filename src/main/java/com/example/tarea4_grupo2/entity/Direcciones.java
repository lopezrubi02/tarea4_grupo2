package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "direcciones")
public class Direcciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int iddirecciones;
    @NotBlank(message = "La direccion no puede ser nulo")
    private String direccion;
//    private String distrito;
    private int activo;

    @ManyToOne
    @JoinColumn(name = "iddistrito")
    private Distritos distrito;

    @ManyToOne
    @JoinColumn(name = "usuariosidusuarios")
    private Usuario usuario;

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }
}
