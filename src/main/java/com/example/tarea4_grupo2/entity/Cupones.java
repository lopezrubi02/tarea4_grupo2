package com.example.tarea4_grupo2.entity;


import org.springframework.format.annotation.NumberFormat;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.sql.Date;

@Entity
@Table(name="cupones")
public class Cupones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcupones;

    @NotBlank(message = "El nombre no puede estar vacio.")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La descripcion no puede estar vacia.")
    @Column(nullable = false)
    private String descripcion;

    @Positive(message = "Este valor no puede ser un numero negativo.")
    @Digits(integer = 8 , fraction=2, message = "Este valor solo puede tener 2 decimales como maximo.")
    @Column(nullable = false)
    private float valordescuento;

    @NotNull(message = "Elija una fecha de inicio")
    @Column(nullable = false)
    private java.sql.Date fechainicio;

    @NotNull(message = "Elija una fecha de fin")
    @Column(nullable = false)
    private java.sql.Date fechafin;

    @ManyToOne
    @JoinColumn(name="idrestaurante")
    private Restaurante restaurante;

    @ManyToOne
    @JoinColumn(name="idplato")
    private Plato plato;

    public int getIdcupones() {
        return idcupones;
    }

    public void setIdcupones(int idcupones) {
        this.idcupones = idcupones;
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

    public float getValordescuento() {
        return valordescuento;
    }

    public void setValordescuento(float valordescuento) {
        this.valordescuento = valordescuento;
    }

    public java.sql.Date getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(java.sql.Date fechainicio) {
        this.fechainicio = fechainicio;
    }

    public java.sql.Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(Date fechafin) {
        this.fechafin = fechafin;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }
}
