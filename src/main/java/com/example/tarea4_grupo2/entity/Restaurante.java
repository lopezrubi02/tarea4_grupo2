package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "restaurante")
public class Restaurante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idrestaurante;
    @Column(nullable = false)
    private String direccion;
    @Column(nullable = false)
    private String distrito;
    @Column(nullable = false)
    private String ruc;
    @Column(nullable = false)
    private String nombre;
    private Float calificacionpromedio;
    @ManyToOne
    @JoinColumn(name="idadminrest")
    private Usuario usuario;
    private byte[] foto;
    @ManyToMany
    @JoinTable(name="restaurante_has_categorias",
            joinColumns = @JoinColumn(name="restaurante_idrestaurante"),
            inverseJoinColumns = @JoinColumn(name="categorias_idcategorias"))
    private List<Categorias> categoriasrestList;

    public Integer getIdrestaurante() {
        return idrestaurante;
    }

    public void setIdrestaurante(Integer idrestaurante) {
        this.idrestaurante = idrestaurante;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Float getCalificacionpromedio() {
        return calificacionpromedio;
    }

    public void setCalificacionpromedio(Float calificacionpromedio) {
        this.calificacionpromedio = calificacionpromedio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public List<Categorias> getCategoriasrestList() {
        return categoriasrestList;
    }

    public void setCategoriasrestList(List<Categorias> categoriasrestList) {
        this.categoriasrestList = categoriasrestList;
    }

    private String fotonombre;
    private String fotocontenttype;
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

}
