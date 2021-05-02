package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name ="pedidos")
public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idpedidos;

    @Column(nullable = false)
    private float montototal;
    private String idrestaurante;

    @Column(nullable = false)
    private int comisionrepartidor;

    @Column(nullable = false)
    private int comisionsistema;

    private String montoexacto;

    @Column(nullable = false)
    private int idmetodopago;
    private int calificacionrestaurante;
    private int calificacionrepartidor;

    @Size(max=200,message = "El nombre no puede tener más de 200 caracteres")
    private String comentario;

    private float tiempodelivery;

    @Column(nullable = false)
    private String estado_restaurante;

    @Column(nullable = false)
    private String estado_repartidor;

    @Column(nullable = false)
    private int idcliente;

    @Column(nullable = false)
    private int idrepartidor;

    public int getIdpedidos() {
        return idpedidos;
    }

    public void setIdpedidos(int idpedidos) {
        this.idpedidos = idpedidos;
    }

    public float getMontototal() {
        return montototal;
    }

    public void setMontototal(float montototal) {
        this.montototal = montototal;
    }

    public String getIdrestaurante() {
        return idrestaurante;
    }

    public void setIdrestaurante(String idrestaurante) {
        this.idrestaurante = idrestaurante;
    }

    public int getComisionrepartidor() {
        return comisionrepartidor;
    }

    public void setComisionrepartidor(int comisionrepartidor) {
        this.comisionrepartidor = comisionrepartidor;
    }

    public int getComisionsistema() {
        return comisionsistema;
    }

    public void setComisionsistema(int comisionsistema) {
        this.comisionsistema = comisionsistema;
    }

    public String getMontoexacto() {
        return montoexacto;
    }

    public void setMontoexacto(String montoexacto) {
        this.montoexacto = montoexacto;
    }

    public int getIdmetodopago() {
        return idmetodopago;
    }

    public void setIdmetodopago(int idmetodopago) {
        this.idmetodopago = idmetodopago;
    }

    public int getCalificacionrestaurante() {
        return calificacionrestaurante;
    }

    public void setCalificacionrestaurante(int calificacionrestaurante) {
        this.calificacionrestaurante = calificacionrestaurante;
    }

    public int getCalificacionrepartidor() {
        return calificacionrepartidor;
    }

    public void setCalificacionrepartidor(int calificacionrepartidor) {
        this.calificacionrepartidor = calificacionrepartidor;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public float getTiempodelivery() {
        return tiempodelivery;
    }

    public void setTiempodelivery(float tiempodelivery) {
        this.tiempodelivery = tiempodelivery;
    }

    public String getEstado_restaurante() {
        return estado_restaurante;
    }

    public void setEstado_restaurante(String estado_restaurante) {
        this.estado_restaurante = estado_restaurante;
    }

    public String getEstado_repartidor() {
        return estado_repartidor;
    }

    public void setEstado_repartidor(String estado_repartidor) {
        this.estado_repartidor = estado_repartidor;
    }

    public int getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(int idcliente) {
        this.idcliente = idcliente;
    }

    public int getIdrepartidor() {
        return idrepartidor;
    }

    public void setIdrepartidor(int idrepartidor) {
        this.idrepartidor = idrepartidor;
    }
}
