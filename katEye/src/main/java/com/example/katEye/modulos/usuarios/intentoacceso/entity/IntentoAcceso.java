package com.example.katEye.modulos.usuarios.intentoacceso.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "intento_acceso")
public class IntentoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoIntento tipo;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "exitoso", nullable = false)
    private boolean exitoso;

    @Column(name = "fecha", nullable = false)
    private Instant fecha = Instant.now();

    protected IntentoAcceso() {
    }

    public IntentoAcceso(
            Long usuarioId,
            TipoIntento tipo,
            String correo,
            String ip,
            boolean exitoso) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.correo = correo;
        this.ip = ip;
        this.exitoso = exitoso;
        this.fecha = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public TipoIntento getTipo() {
        return tipo;
    }

    public String getCorreo() {
        return correo;
    }

    public String getIp() {
        return ip;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public Instant getFecha() {
        return fecha;
    }
}
