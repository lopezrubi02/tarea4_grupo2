package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name ="pedidos")
public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idpedidos;

    @OneToMany(mappedBy = "pedido")
    private Set<PedidoHasPlato> pedidohasplato = new HashSet<>();

    private float montototal;

    private int comisionrepartidor;

    private int comisionsistema;

    private String montoexacto;

    private int idmetodopago;

    private int calificacionrestaurante;

    private int calificacionrepartidor;

    @Size(max=200,message = "El comentario no puede tener más de 200 caracteres")
    private String comentario;

    private float tiempodelivery;

    private String estadorestaurante;

    private String estadorepartidor;

    @Column(nullable = false)
    private int idcliente;


    private int idrepartidor;

    @Column(nullable = false)
    private int restaurante_idrestaurante;

    private Date fechahorapedido;
    private Date fechahoraentregado;
    private int direccionentrega;

    public Set<PedidoHasPlato> getPedidohasplato() {
        return pedidohasplato;
    }

    public void setPedidohasplato(Set<PedidoHasPlato> pedidohasplato) {
        this.pedidohasplato = pedidohasplato;
    }

    public void addpedido(PedidoHasPlato pedidoHasPlato){
        this.pedidohasplato.add(pedidoHasPlato);
    }


    public Pedidos() {
    }

    public Pedidos(float montototal, int comisionrepartidor, int comisionsistema, String montoexacto, int idmetodopago, int calificacionrestaurante, int calificacionrepartidor, String comentario, float tiempodelivery, String estadorestaurante, String estadorepartidor, int idcliente, int idrepartidor, int restaurante_idrestaurante, Date fechahorapedido, Date fechahoraentregado, int direccionentrega) {
        this.montototal = montototal;
        this.comisionrepartidor = comisionrepartidor;
        this.comisionsistema = comisionsistema;
        this.montoexacto = montoexacto;
        this.idmetodopago = idmetodopago;
        this.calificacionrestaurante = calificacionrestaurante;
        this.calificacionrepartidor = calificacionrepartidor;
        this.comentario = comentario;
        this.tiempodelivery = tiempodelivery;
        this.estadorestaurante = estadorestaurante;
        this.estadorepartidor = estadorepartidor;
        this.idcliente = idcliente;
        this.idrepartidor = idrepartidor;
        this.restaurante_idrestaurante = restaurante_idrestaurante;
        this.fechahorapedido = fechahorapedido;
        this.fechahoraentregado = fechahoraentregado;
        this.direccionentrega = direccionentrega;
    }

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

    public String getEstadorestaurante() {
        return estadorestaurante;
    }

    public void setEstadorestaurante(String estadorestaurante) {
        this.estadorestaurante = estadorestaurante;
    }

    public String getEstadorepartidor() {
        return estadorepartidor;
    }

    public void setEstadorepartidor(String estadorepartidor) {
        this.estadorepartidor = estadorepartidor;
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

    public int getRestaurante_idrestaurante() {
        return restaurante_idrestaurante;
    }

    public void setRestaurante_idrestaurante(int restaurante_idrestaurante) {
        this.restaurante_idrestaurante = restaurante_idrestaurante;
    }

    public Date getFechahorapedido() {
        return fechahorapedido;
    }

    public void setFechahorapedido(Date fechahorapedido) {
        this.fechahorapedido = fechahorapedido;
    }

    public Date getFechahoraentregado() {
        return fechahoraentregado;
    }

    public void setFechahoraentregado(Date fechahoraentregado) {
        this.fechahoraentregado = fechahoraentregado;
    }

    public int getDireccionentrega() {
        return direccionentrega;
    }

    public void setDireccionentrega(int direccionentrega) {
        this.direccionentrega = direccionentrega;
    }
}
