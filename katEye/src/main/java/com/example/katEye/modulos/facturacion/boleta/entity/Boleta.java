package com.example.katEye.modulos.facturacion.boleta.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.example.katEye.modulos.facturacion.detalleboleta.entity.DetalleBoleta;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boleta")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false, unique = true)
    private Long ventaId;

    @Column(name = "folio", length = 30, nullable = false, unique = true)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 10, nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "rut_receptor", length = 12)
    private String rutReceptor;

    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision;

    @Column(name = "monto_neto", precision = 10, scale = 0, nullable = false)
    private BigDecimal montoNeto;

    @Column(name = "monto_iva", precision = 10, scale = 0, nullable = false)
    private BigDecimal montoIva;

    @Column(name = "total", precision = 10, scale = 0, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    private EstadoBoleta estado;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    protected Boleta() {
    }

    public Boleta(
            Long ventaId,
            String folio,
            TipoDocumento tipoDocumento,
            String rutReceptor,
            Instant fechaEmision,
            BigDecimal montoNeto,
            BigDecimal montoIva,
            BigDecimal total,
            EstadoBoleta estado) {
        this.ventaId = ventaId;
        this.folio = folio;
        this.tipoDocumento = tipoDocumento;
        this.rutReceptor = rutReceptor;
        this.fechaEmision = fechaEmision;
        this.montoNeto = montoNeto;
        this.montoIva = montoIva;
        this.total = total;
        this.estado = estado;
    }

    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.asignarBoleta(this);
        detalles.add(detalle);
    }

    public Long getId() {
        return id;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public String getFolio() {
        return folio;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getRutReceptor() {
        return rutReceptor;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public BigDecimal getMontoNeto() {
        return montoNeto;
    }

    public BigDecimal getMontoIva() {
        return montoIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoBoleta getEstado() {
        return estado;
    }

    public List<DetalleBoleta> getDetalles() {
        return detalles;
    }

    public void anular() {
        this.estado = EstadoBoleta.ANULADA;
    }
}
