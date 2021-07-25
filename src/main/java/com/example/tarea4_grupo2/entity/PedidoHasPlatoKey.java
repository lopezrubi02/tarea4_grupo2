package com.example.tarea4_grupo2.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PedidoHasPlatoKey implements Serializable {

    @Column(name = "pedidosidpedidos")
    private int pedidosidpedidos;

    @Column(name = "platoidplato")
    private String platoidplato;

    public PedidoHasPlatoKey() {
    }

    public PedidoHasPlatoKey(int pedidosidpedidos, String platoidplato) {
        super();
        this.pedidosidpedidos = pedidosidpedidos;
        this.platoidplato = platoidplato;
    }

    public int getPedidosidpedidos() {
        return pedidosidpedidos;
    }

    public void setPedidosidpedidos(int pedidosidpedidos) {
        this.pedidosidpedidos = pedidosidpedidos;
    }

    public String getPlatoidplato() {
        return platoidplato;
    }

    public void setPlatoidplato(String platoidplato) {
        this.platoidplato = platoidplato;
    }
}
