package com.example.katEye.modulos.carrito.carritoitem.entity;

import com.example.katEye.modulos.carrito.carrito.entity.Carrito;
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
@Table(name = "carrito_item")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false)
    private Short cantidad;

    protected CarritoItem() {
    }

    public CarritoItem(Carrito carrito, Long productoId, Short cantidad) {
        this.carrito = carrito;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Short getCantidad() {
        return cantidad;
    }

    public void cambiarCantidad(Short cantidad) {
        this.cantidad = cantidad;
    }
}
