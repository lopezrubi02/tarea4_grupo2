package com.example.tarea4_grupo2.entity;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

// TODO validaciones de No Nulo, etc

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idusuarios;

    @Column(nullable = false)
    @NotBlank(message = "No puede estar vacío")
    @Size(max=45,message = "Los nombres no puede tener más de 45 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñ ]*$",message = "Solo puede contener letras")
    private String nombre;

    @Column(nullable = false)
    @NotBlank(message = "No puede estar vacío")
    @Size(max=45,message = "Los apellidos no puede tener más de 45 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñ ]*$",message = "Solo puede contener letras")
    private String apellidos;

    @Column(nullable = true)
    private String token;

    @Column(nullable = false)
    @NotBlank(message = "No puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",message = "Formato de correo inválido.")
    @Size(max=45, message = "El email no puede tener más de 45 caracteres")
    private String email;

    @Column(name = "contraseniahash",nullable = false)
    @NotBlank(message = "No puede estar vacío")
    private String contraseniaHash;

    @Column(nullable = false)
    @NotNull(message = "No puede estar vacío")
    @Min(value=111111111,message="El teléfono debe tener 9 dígitos")
    @Max(value=999999999,message="El teléfono debe tener 9 dígitos")
    private Integer telefono;

    @Column(name = "fechanacimiento",nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "No puede estar vacío")
    private Date fechaNacimiento;

    @Column(nullable = false)
    @NotBlank(message = "No puede estar vacío")
    private String sexo;

    @Column(nullable = false)
    @NotBlank(message = "No puede estar vacío")
    @Size(max=8,message = "No puede tener más de 8 dígitos")
    @Size(min=8,message = "No puede tener menos de 8 dígitos")
    @Pattern(regexp = "^[0-9]*$",message = "Solo puede contener números")
    private String dni;

    private Integer comisionventa;

    @Column(nullable = false)
    private String rol;

    @Column(name = "cuentaactiva")
    private Integer cuentaActiva;

    private LocalDateTime ultimafechaingreso;

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

    public LocalDateTime getUltimafechaingreso() {
        return ultimafechaingreso;
    }

    public void setUltimafechaingreso(LocalDateTime ultimafechaingreso) {
        this.ultimafechaingreso = ultimafechaingreso;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
