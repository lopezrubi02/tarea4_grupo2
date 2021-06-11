package com.example.tarea4_grupo2.entity;

import javax.persistence.*;

@Entity
@Table(name="fotosplatos")
public class FotosPlatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idfotosplatos;

    private byte[] foto;
    private String fotonombre;
    private String fotocontenttype;

    @ManyToOne
    @JoinColumn(name="idplato")
    private Plato idplato;

    public int getIdfotosplatos() {
        return idfotosplatos;
    }

    public void setIdfotosplatos(int idfotosplatos) {
        this.idfotosplatos = idfotosplatos;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getFotonombre() {
        return fotonombre;
    }

    public void setFotonombre(String fotonombre) {
        this.fotonombre = fotonombre;
    }

    public String getFotocontenttype() {
        return fotocontenttype;
    }

    public void setFotocontenttype(String fotocontenttype) {
        this.fotocontenttype = fotocontenttype;
    }

    public Plato getIdplato() {
        return idplato;
    }

    public void setIdplato(Plato idplato) {
        this.idplato = idplato;
    }
}
