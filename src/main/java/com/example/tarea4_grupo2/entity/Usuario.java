package com.example.tarea4_grupo2.entity;

import javax.persistence.*;
import java.util.Date;

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
    private String contraseniahash;
    private Integer telefono;
    private String sexo;
    private String dni;
    private Integer comisionventa;
    private String rol;
    private Integer cuentaactiva;
    private Date fechanacimiento;

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

    public String getContraseniahash() {
        return contraseniahash;
    }

    public void setContraseniahash(String contrasenia_hash) {
        this.contraseniahash = contrasenia_hash;
    }

    public Integer getCuentaactiva() {
        return cuentaactiva;
    }

    public void setCuentaactiva(Integer cuenta_activa) {
        this.cuentaactiva = cuenta_activa;
    }

    public Date getFechanacimiento() {
        return fechanacimiento;
    }

    public void setFechanacimiento(Date fecha_nacimiento) {
        this.fechanacimiento = fecha_nacimiento;
    }
}
