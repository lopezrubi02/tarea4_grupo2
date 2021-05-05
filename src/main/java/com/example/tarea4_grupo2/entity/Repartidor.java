package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.sql.Blob;

@Entity
@Table(name = "datosrepartidor")
public class Repartidor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idrepartidor;

    private String movilidad;
    private String placa;
    private String licencia;
    private Byte foto;
    private boolean disponibilidad;
    private Float calificacionpromedio;

    private String distritoactual;

    @Column(name="usuarios_idusuarios")
    private int usuariosIdusuarios;



    public int getIdrepartidor() {
        return idrepartidor;
    }

    public void setIdrepartidor(int idrepartidor) {
        this.idrepartidor = idrepartidor;
    }

    public String getMovilidad() {
        return movilidad;
    }

    public void setMovilidad(String movilidad) {
        this.movilidad = movilidad;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public int getUsuariosIdusuarios() {
        return usuariosIdusuarios;
    }

    public void setUsuariosIdusuarios(int usuariosIdusuarios) {
        this.usuariosIdusuarios = usuariosIdusuarios;
    }

    public String getDistritoactual() {
        return distritoactual;
    }

    public void setDistritoactual(String distritoActual) {
        this.distritoactual = distritoActual;
    }
}
