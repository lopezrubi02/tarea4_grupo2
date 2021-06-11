package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name = "tarjetasonline")
public class TarjetasOnline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idtarjetasonline;

    @Column(nullable = false)
    private String numerotarjeta;

    @ManyToOne
    @JoinColumn(name = "usuariosidusuarios")
    private Usuario cliente;

    public Integer getIdtarjetasonline() {
        return idtarjetasonline;
    }

    public void setIdtarjetasonline(Integer idtarjetasonline) {
        this.idtarjetasonline = idtarjetasonline;
    }

    public String getNumerotarjeta() {
        return numerotarjeta;
    }

    public void setNumerotarjeta(String numerotarjeta) {
        this.numerotarjeta = numerotarjeta;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }
}
