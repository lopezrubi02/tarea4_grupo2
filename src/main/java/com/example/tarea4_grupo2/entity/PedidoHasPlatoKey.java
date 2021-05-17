package com.example.tarea4_grupo2.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PedidoHasPlatoKey implements Serializable {

    @Column(name = "pedidos_idpedidos")
    private int pedidosidpedidos;

    @Column(name = "plato_idplato")
    private int platoidplato;


    public int getPedidosidpedidos() {
        return pedidosidpedidos;
    }

    public void setPedidosidpedidos(int pedidosidpedidos) {
        this.pedidosidpedidos = pedidosidpedidos;
    }

    public int getPlatoidplato() {
        return platoidplato;
    }

    public void setPlatoidplato(int platoidplato) {
        this.platoidplato = platoidplato;
    }
}
