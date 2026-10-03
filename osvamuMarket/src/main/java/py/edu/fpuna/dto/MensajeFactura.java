package py.edu.fpuna.dto;

import py.edu.fpuna.entities.Factura;

public class MensajeFactura {
    private Factura factura;
    private String detalle;

    public MensajeFactura(Factura factura, String detalle) {
        this.factura = factura;
        this.detalle = detalle;
    }

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}