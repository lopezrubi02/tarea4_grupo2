package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
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
    private String fotonombre;
    private String fotocontenttype;
    private byte[] foto;

    @Column(name ="usuariosidusuarios")
    private int idusuarios;

    private boolean disponibilidad;
    private Float calificacionpromedio;

    public Distritos getDistritos() {
        return distritos;
    }

    public void setDistritos(Distritos distritos) {
        this.distritos = distritos;
    }

    @ManyToOne
    @JoinColumn(name = "iddistritoactual")
    private Distritos distritos;

    public String getFotonombre() {
        return fotonombre;
    }

    public String getFotocontenttype() {
        return fotocontenttype;
    }

    public byte[] getFoto() {
        return foto;
    }

   /*@OneToOne
    @PrimaryKeyJoinColumn(name = "usuarios_idusuarios")
    Usuario usuarios;
*/
    public void setFotonombre(String fotonombre) {
        this.fotonombre = fotonombre;
    }

    public void setFotocontenttype(String fotocontenttype) {
        this.fotocontenttype = fotocontenttype;
    }

    public void setFoto(byte[] foto) {
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
}
