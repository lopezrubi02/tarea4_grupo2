package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name="plato")
public class Platos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idplato;

    private String nombre;
    private String descripcion;
    private Float precio;

    @ManyToOne
    @JoinColumn(name =  "cupones_idcupones")
    private Cupones cupones;

    @ManyToOne
    @JoinColumn(name = "restaurante_idrestaurante")
    private Restaurante restaurante;

    private int disponibilidad;


    public int getIdplato() {
        return idplato;
    }

    public void setIdplato(int idplato) {
        this.idplato = idplato;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public Cupones getCupones() {
        return cupones;
    }

    public void setCupones(Cupones cupones) {
        this.cupones = cupones;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public int getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(int disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}
