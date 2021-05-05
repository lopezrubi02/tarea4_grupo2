package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.text.DateFormat;

@Entity
@Table(name = "cupones")
public class Cupones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcupones;

    private String nombre;
    private String descripcion;

    private Float valordescuento;
    private DateFormat fechainicio;
    private DateFormat fechafin;

    @ManyToOne
    @JoinColumn(name = "restaurante_idrestaurante")
    private Restaurante restaurante;


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

    public Float getValordescuento() {
        return valordescuento;
    }

    public void setValordescuento(Float valordescuento) {
        this.valordescuento = valordescuento;
    }

    public DateFormat getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(DateFormat fechainicio) {
        this.fechainicio = fechainicio;
    }

    public DateFormat getFechafin() {
        return fechafin;
    }

    public void setFechafin(DateFormat fechafin) {
        this.fechafin = fechafin;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }
}
