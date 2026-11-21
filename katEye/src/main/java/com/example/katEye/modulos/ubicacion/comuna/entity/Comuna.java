package com.example.katEye.modulos.ubicacion.comuna.entity;

import com.example.katEye.modulos.ubicacion.region.entity.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comuna")
public class Comuna {

    @Id
    @Column(name = "codigo", length = 10, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_codigo", nullable = false)
    private Region region;

    protected Comuna() {
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Region getRegion() {
        return region;
    }
}
