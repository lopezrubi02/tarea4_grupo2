package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name = "metodospago")
public class MetodosDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idmetodospago;

    private String metodo;

    public int getIdmetodospago() {
        return idmetodospago;
    }

    public void setIdmetodospago(int idmetodospago) {
        this.idmetodospago = idmetodospago;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }
}
