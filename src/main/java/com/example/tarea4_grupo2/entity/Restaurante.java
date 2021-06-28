package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurante")
public class Restaurante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idrestaurante;
    @Column(nullable = false)
    @NotBlank(message = "No puede estar en blanco")
    private String direccion;
    @Column(nullable = false)
    @NotBlank(message = "No puede estar en blanco")
    @Pattern(regexp = "^[0-9]*$",message = "Solo pueden ser numeros")
    @Size(max=11,message = "No puede tenr más de 11 dígitos")
    @Size(min=11,message = "No puede tenr menos de 11 dígitos")
    private String ruc;
    @Column(nullable = false)
    @NotBlank(message = "No puede estar en blanco")
    private String nombre;
    private Float calificacionpromedio;
    @ManyToOne
    @JoinColumn(name="idadminrest")
    private Usuario usuario;
    private byte[] foto;
    private String fotonombre;
    private String fotocontenttype;

    @ManyToOne
    @JoinColumn(name="iddistrito")
    private Distritos distrito;

    @ManyToMany
    @JoinTable(name="restaurantehascategorias",
            joinColumns = @JoinColumn(name="restaurantesidrestaurantes"),
            inverseJoinColumns = @JoinColumn(name="categoriasidcategorias"))
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

    public Distritos getDistrito() {
        return distrito;
    }

    public void setDistrito(Distritos distrito) {
        this.distrito = distrito;
    }

    public List<Categorias> getCategoriasrestList() {
        return categoriasrestList;
    }

    public void setCategoriasrestList(List<Categorias> categoriasrestList) {
        this.categoriasrestList = categoriasrestList;
    }
}