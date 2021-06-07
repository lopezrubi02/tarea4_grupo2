package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Entity
@Table(name ="pedidos")
public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idpedidos;

    @OneToMany(mappedBy = "pedido")
    private Set<PedidoHasPlato> pedidohasplato = new HashSet<>();

    private float montototal;

    private int comisionrepartidor;

    private int comisionsistema;

    private String montoexacto;

    @ManyToOne
    @JoinColumn(name = "idmetodopago")
    private MetodosDePago metododepago;

    private int calificacionrestaurante;

    private int calificacionrepartidor;

    @Size(max=200,message = "El comentario no puede tener más de 200 caracteres")
    private String comentario;

    private float tiempodelivery;

    private String estadorestaurante;

    private String estadorepartidor;

    @Column(nullable = false)
    private int idcliente;

    @ManyToOne
    @JoinColumn(name = "idrepartidor")
    private Usuario repartidor;

    @ManyToOne
    @JoinColumn(name = "restaurante_idrestaurante")
    private Restaurante restaurantepedido;

    private Date fechahorapedido;
    private Date fechahoraentregado;

    @ManyToOne
    @JoinColumn(name = "direccionentrega")
    private Direcciones direccionentrega;

    public void removePlato(Plato plato){
        for(Iterator<PedidoHasPlato> iterator = plato.getPedidohasplato().iterator();
                    iterator.hasNext();){
            PedidoHasPlato pedidoHasPlato1 = iterator.next();
            if(pedidoHasPlato1.getPedido().equals(this) && pedidoHasPlato1.getPlato().equals(plato)){
                iterator.remove();
                pedidoHasPlato1.setPlato(null);
                pedidoHasPlato1.setPedido(null);
            }
        }
    }

    public Set<PedidoHasPlato> getPedidohasplato() {
        return pedidohasplato;
    }

    public void setPedidohasplato(Set<PedidoHasPlato> pedidohasplato) {
        this.pedidohasplato = pedidohasplato;
    }

    public void addpedido(PedidoHasPlato pedidoHasPlato){
        this.pedidohasplato.add(pedidoHasPlato);
    }



    public Pedidos() {
    }

    public Pedidos(int idpedidos, Set<PedidoHasPlato> pedidohasplato, float montototal, int comisionrepartidor, int comisionsistema, String montoexacto, MetodosDePago metododepago, int calificacionrestaurante, int calificacionrepartidor, String comentario, float tiempodelivery, String estadorestaurante, String estadorepartidor, int idcliente, Usuario repartidor, Restaurante restaurantepedido, Date fechahorapedido, Date fechahoraentregado, Direcciones direccionentrega) {
        this.idpedidos = idpedidos;
        this.pedidohasplato = pedidohasplato;
        this.montototal = montototal;
        this.comisionrepartidor = comisionrepartidor;
        this.comisionsistema = comisionsistema;
        this.montoexacto = montoexacto;
        this.metododepago = metododepago;
        this.calificacionrestaurante = calificacionrestaurante;
        this.calificacionrepartidor = calificacionrepartidor;
        this.comentario = comentario;
        this.tiempodelivery = tiempodelivery;
        this.estadorestaurante = estadorestaurante;
        this.estadorepartidor = estadorepartidor;
        this.idcliente = idcliente;
        this.repartidor = repartidor;
        this.restaurantepedido = restaurantepedido;
        this.fechahorapedido = fechahorapedido;
        this.fechahoraentregado = fechahoraentregado;
        this.direccionentrega = direccionentrega;
    }

    public int getIdpedidos() {
        return idpedidos;
    }

    public void setIdpedidos(int idpedidos) {
        this.idpedidos = idpedidos;
    }

    public float getMontototal() {
        return montototal;
    }

    public void setMontototal(float montototal) {
        this.montototal = montototal;
    }

    public int getComisionrepartidor() {
        return comisionrepartidor;
    }

    public void setComisionrepartidor(int comisionrepartidor) {
        this.comisionrepartidor = comisionrepartidor;
    }

    public int getComisionsistema() {
        return comisionsistema;
    }

    public void setComisionsistema(int comisionsistema) {
        this.comisionsistema = comisionsistema;
    }

    public String getMontoexacto() {
        return montoexacto;
    }

    public void setMontoexacto(String montoexacto) {
        this.montoexacto = montoexacto;
    }

    public MetodosDePago getMetododepago() {
        return metododepago;
    }

    public void setMetododepago(MetodosDePago metododepago) {
        this.metododepago = metododepago;
    }

    public int getCalificacionrestaurante() {
        return calificacionrestaurante;
    }

    public void setCalificacionrestaurante(int calificacionrestaurante) {
        this.calificacionrestaurante = calificacionrestaurante;
    }

    public int getCalificacionrepartidor() {
        return calificacionrepartidor;
    }

    public void setCalificacionrepartidor(int calificacionrepartidor) {
        this.calificacionrepartidor = calificacionrepartidor;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public float getTiempodelivery() {
        return tiempodelivery;
    }

    public void setTiempodelivery(float tiempodelivery) {
        this.tiempodelivery = tiempodelivery;
    }

    public String getEstadorestaurante() {
        return estadorestaurante;
    }

    public void setEstadorestaurante(String estadorestaurante) {
        this.estadorestaurante = estadorestaurante;
    }

    public String getEstadorepartidor() {
        return estadorepartidor;
    }

    public void setEstadorepartidor(String estadorepartidor) {
        this.estadorepartidor = estadorepartidor;
    }

    public int getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(int idcliente) {
        this.idcliente = idcliente;
    }

    public Usuario getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(Usuario repartidor) {
        this.repartidor = repartidor;
    }

    public Restaurante getRestaurantepedido() {
        return restaurantepedido;
    }

    public void setRestaurantepedido(Restaurante restaurantepedido) {
        this.restaurantepedido = restaurantepedido;
    }

    public Date getFechahorapedido() {
        return fechahorapedido;
    }

    public void setFechahorapedido(Date fechahorapedido) {
        this.fechahorapedido = fechahorapedido;
    }

    public Date getFechahoraentregado() {
        return fechahoraentregado;
    }

    public void setFechahoraentregado(Date fechahoraentregado) {
        this.fechahoraentregado = fechahoraentregado;
    }

    public Direcciones getDireccionentrega() {
        return direccionentrega;
    }

    public void setDireccionentrega(Direcciones direccionentrega) {
        this.direccionentrega = direccionentrega;
    }
}
