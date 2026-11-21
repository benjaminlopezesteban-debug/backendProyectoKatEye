package com.example.katEye.modulos.pedidos.pedido.entity;

import com.example.katEye.modulos.pedidos.pedidoproducto.entity.PedidoProducto;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", length = 20, nullable = false, unique = true)
    private String numero;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", length = 20, nullable = false)
    private TipoEntrega tipoEntrega;

    @Column(name = "contacto_nombre", length = 100, nullable = false)
    private String contactoNombre;

    @Column(name = "contacto_correo", length = 100, nullable = false)
    private String contactoCorreo;

    @Column(name = "contacto_telefono", length = 20, nullable = false)
    private String contactoTelefono;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "region_codigo", length = 10)
    private String regionCodigo;

    @Column(name = "comuna_codigo", length = 10)
    private String comunaCodigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 20, nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 25, nullable = false)
    private EstadoPedido estado;

    @Column(name = "subtotal", precision = 10, scale = 0, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "ahorro", precision = 10, scale = 0, nullable = false)
    private BigDecimal ahorro;

    @Column(name = "costo_despacho", precision = 10, scale = 0, nullable = false)
    private BigDecimal costoDespacho;

    @Column(name = "total", precision = 10, scale = 0, nullable = false)
    private BigDecimal total;

    @Column(name = "transportista", length = 100)
    private String transportista;

    @Column(name = "codigo_seguimiento", length = 60)
    private String codigoSeguimiento;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "expira_en")
    private Instant expiraEn;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> lineas = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(
            String numero,
            Long usuarioId,
            TipoEntrega tipoEntrega,
            String contactoNombre,
            String contactoCorreo,
            String contactoTelefono,
            String direccion,
            String regionCodigo,
            String comunaCodigo,
            MetodoPago metodoPago,
            BigDecimal subtotal,
            BigDecimal ahorro,
            BigDecimal costoDespacho,
            BigDecimal total,
            String idempotencyKey,
            Instant expiraEn) {
        this.numero = numero;
        this.usuarioId = usuarioId;
        this.tipoEntrega = tipoEntrega;
        this.contactoNombre = contactoNombre;
        this.contactoCorreo = contactoCorreo;
        this.contactoTelefono = contactoTelefono;
        this.direccion = direccion;
        this.regionCodigo = regionCodigo;
        this.comunaCodigo = comunaCodigo;
        this.metodoPago = metodoPago;
        this.estado = EstadoPedido.PENDIENTE_PAGO;
        this.subtotal = subtotal;
        this.ahorro = ahorro;
        this.costoDespacho = costoDespacho;
        this.total = total;
        this.idempotencyKey = idempotencyKey;
        this.expiraEn = expiraEn;
    }

    public void agregarLinea(PedidoProducto linea) {
        this.lineas.add(linea);
    }

    public void cambiarEstado(EstadoPedido nuevo) {
        this.estado = nuevo;
    }

    public void asignarSeguimiento(String transportista, String codigoSeguimiento) {
        this.transportista = transportista;
        this.codigoSeguimiento = codigoSeguimiento;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public String getContactoNombre() {
        return contactoNombre;
    }

    public String getContactoCorreo() {
        return contactoCorreo;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getRegionCodigo() {
        return regionCodigo;
    }

    public String getComunaCodigo() {
        return comunaCodigo;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getAhorro() {
        return ahorro;
    }

    public BigDecimal getCostoDespacho() {
        return costoDespacho;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getTransportista() {
        return transportista;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public List<PedidoProducto> getLineas() {
        return lineas;
    }
}
