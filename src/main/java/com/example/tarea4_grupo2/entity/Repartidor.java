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

    @Column(name ="usuarios_idusuarios")
    private int idusuarios;

    @ManyToOne
    @JoinColumn(name = "iddistritoactual")
    private Distritos distrito;

    /*@OneToOne
    @PrimaryKeyJoinColumn(name = "usuarios_idusuarios")
    Usuario usuarios;
*/
    public Byte getFoto() {
        return foto;
    }

    public void setFoto(Byte foto) {
        this.foto = foto;
    }

    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public Float getCalificacionpromedio() {
        return calificacionpromedio;
    }

    public void setCalificacionpromedio(Float calificacionpromedio) {
        this.calificacionpromedio = calificacionpromedio;
    }

    private boolean disponibilidad;
    private Float calificacionpromedio;


    //private String distritoactual;


/*    public Usuario getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Usuario usuarios) {
        this.usuarios = usuarios;
    }
*/

    public int getIdusuarios() {
        return idusuarios;
    }

    public void setIdusuarios(int idusuarios) {
        this.idusuarios = idusuarios;
    }

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


    public Distritos getDistrito() {
        return distrito;
    }

    public void setDistrito(Distritos distrito) {
        this.distrito = distrito;
    }
}
