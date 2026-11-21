package com.example.katEye.modulos.contenido.mensajecontacto.entity;

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
@Table(name = "mensaje_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "respondido_por")
    private Long respondidoPor;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "correo", length = 100, nullable = false)
    private String correo;

    @Column(name = "comentario", length = 500, nullable = false)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    private EstadoMensaje estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected MensajeContacto() {
    }

    public MensajeContacto(String nombre, String correo, String comentario, Instant creadoEn) {
        this.nombre = nombre;
        this.correo = correo;
        this.comentario = comentario;
        this.estado = EstadoMensaje.NUEVO;
        this.creadoEn = creadoEn;
    }

    public void cambiarEstado(EstadoMensaje nuevo, Long administradorId) {
        this.estado = nuevo;
        if (nuevo == EstadoMensaje.RESPONDIDO) {
            this.respondidoPor = administradorId;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getRespondidoPor() {
        return respondidoPor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getComentario() {
        return comentario;
    }

    public EstadoMensaje getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
