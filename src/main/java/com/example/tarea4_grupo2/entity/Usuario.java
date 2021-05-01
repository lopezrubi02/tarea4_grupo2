package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.util.Date;

// TODO atributo fecha
// TODO validaciones de No Nulo, etc

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idusuarios;
    private String nombre;
    private String apellidos;
    private String email;
    private String contraseniaHash;
    private Integer telefono;
    private String sexo;
    private String dni;
    private Integer comisionventa;
    private String rol;
    private Integer cuentaActiva;
    private Date fechaNacimiento;

    public Integer getIdusuarios() {
        return idusuarios;
    }

    public void setIdusuarios(Integer idusuarios) {
        this.idusuarios = idusuarios;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseniaHash() {
        return contraseniaHash;
    }

    public void setContraseniaHash(String contraseniaHash) {
        this.contraseniaHash = contraseniaHash;
    }

    public Integer getTelefono() {
        return telefono;
    }

    public void setTelefono(Integer telefono) {
        this.telefono = telefono;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Integer getComisionventa() {
        return comisionventa;
    }

    public void setComisionventa(Integer comisionventa) {
        this.comisionventa = comisionventa;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getCuentaActiva() {
        return cuentaActiva;
    }

    public void setCuentaActiva(Integer cuentaActiva) {
        this.cuentaActiva = cuentaActiva;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
