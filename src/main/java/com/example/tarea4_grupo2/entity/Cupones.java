package com.example.tarea4_grupo2.entity;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="cupones")
public class Cupones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcupones;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private float valordescuento;

    @Column(nullable = false)
    private Date fechainicio;

    @Column(nullable = false)
    private Date fechafin;

    @ManyToOne
    @JoinColumn(name="restaurante_idrestaurante")
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

    public float getValordescuento() {
        return valordescuento;
    }

    public void setValordescuento(float valordescuento) {
        this.valordescuento = valordescuento;
    }

    public Date getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(Date fechainicio) {
        this.fechainicio = fechainicio;
    }

    public Date getFechafin() {
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
}
