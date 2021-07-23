package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="plato")
public class Plato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idplato;

    @OneToMany(mappedBy = "pedido")
    private Set<PedidoHasPlato> pedidohasplato = new HashSet<>();

    @NotBlank(message = "El nombre no puede estar vacio.")
    @Size(max = 30, message = "El nombre no puede sobrepasar los 30 caracteres.")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La descripcion no puede estar vacia.")
    @Size(min = 3, message = "La descripcion debe tener como minimo 3 caracteres.")
    @Size(max = 200, message = "El descripcion no puede sobrepasar los 200 caracteres.")
    private String descripcion;

    @Digits(integer=10, fraction = 2, message = "El precio debe tener 2 decimales como maximo.")
    @Positive(message = "El precio no puede ser un numero negativo.")
    private float precio;

    @ManyToOne
    @JoinColumn(name="restauranteidrestaurante")
    private Restaurante restaurante;

    private int disponibilidad;

    private int activo;

    public Set<PedidoHasPlato> getPedidohasplato() {
        return pedidohasplato;
    }

    public void setPedidohasplato(Set<PedidoHasPlato> pedidohasplato) {
        this.pedidohasplato = pedidohasplato;
    }

    public Plato() {
    }

    public Plato(String nombre, String descripcion, float precio, Restaurante restaurante, int disponibilidad, int activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.restaurante = restaurante;
        this.disponibilidad = disponibilidad;
        this.activo = activo;
    }

    public int getIdplato() {
        return idplato;
    }

    public void setIdplato(int idplato) {
        this.idplato = idplato;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public int getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(int disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }
}
