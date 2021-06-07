package com.example.tarea4_grupo2.entity;


import javax.persistence.*;

@Entity
@Table(name = "Distritos")
public class Distritos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int iddistritos;

    private String nombredistrito;

    public int getIddistritos() {
        return iddistritos;
    }

    public void setIddistritos(int iddistritos) {
        this.iddistritos = iddistritos;
    }

    public String getNombredistrito() {
        return nombredistrito;
    }

    public void setNombredistrito(String nombredistrito) {
        this.nombredistrito = nombredistrito;
    }
}
