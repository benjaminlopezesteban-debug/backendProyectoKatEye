package com.example.katEye.modulos.usuarios.rol.entity;

import com.example.katEye.shared.security.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class RolEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 30, nullable = false, unique = true)
    private String nombre;

    protected RolEntidad() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol aRol() {
        return Rol.valueOf(nombre.toUpperCase());
    }
}
