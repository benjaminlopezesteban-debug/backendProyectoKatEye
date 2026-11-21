package com.example.katEye.modulos.usuarios.usuario.entity;

import com.example.katEye.modulos.usuarios.rol.entity.RolEntidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "correo", length = 100, nullable = false)
    private String correo;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "region_codigo", length = 10, nullable = false)
    private String regionCodigo;

    @Column(name = "comuna_codigo", length = 10, nullable = false)
    private String comunaCodigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntidad rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected Usuario() {
    }

    public Usuario(
            String nombre,
            String correo,
            String passwordHash,
            String telefono,
            String regionCodigo,
            String comunaCodigo,
            RolEntidad rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
        this.regionCodigo = regionCodigo;
        this.comunaCodigo = comunaCodigo;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRegionCodigo() {
        return regionCodigo;
    }

    public String getComunaCodigo() {
        return comunaCodigo;
    }

    public RolEntidad getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void asignarRol(RolEntidad rol) {
        this.rol = rol;
    }

    public void cambiarPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
