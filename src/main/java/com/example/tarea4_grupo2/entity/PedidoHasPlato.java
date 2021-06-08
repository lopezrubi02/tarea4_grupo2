package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Pedidoshasplato")
public class PedidoHasPlato{

    @EmbeddedId
    PedidoHasPlatoKey id;

    @ManyToOne
    @MapsId("pedidosIdpedidos")
    @JoinColumn(name = "pedidosIdpedidos")
    private Pedidos pedido;

    @ManyToOne
    @MapsId("platoIdplato")
    @JoinColumn(name = "platoIdplato")
    private Plato plato;

    private String descripcion;

    private int cantidadplatos;

    private boolean cubiertos;

    public PedidoHasPlato() {
    }

    public PedidoHasPlato(PedidoHasPlatoKey id, Pedidos pedido, Plato plato, String descripcion, int cantidadplatos, boolean cubiertos) {
        this.id = id;
        this.pedido = pedido;
        this.plato = plato;
        this.descripcion = descripcion;
        this.cantidadplatos = cantidadplatos;
        this.cubiertos = cubiertos;
    }

    public PedidoHasPlatoKey getId() {
        return id;
    }

    public void setId(PedidoHasPlatoKey id) {
        this.id = id;
    }

    public Pedidos getPedido() {
        return pedido;
    }

    public void setPedido(Pedidos pedido) {
        this.pedido = pedido;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidadplatos() {
        return cantidadplatos;
    }

    public void setCantidadplatos(int cantidadplatos) {
        this.cantidadplatos = cantidadplatos;
    }

    public boolean isCubiertos() {
        return cubiertos;
    }

    public void setCubiertos(boolean cubiertos) {
        this.cubiertos = cubiertos;
    }
}
